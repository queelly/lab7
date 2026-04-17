package manager;

import network.Request;
import network.Response;
import network.Serializer;
import manager.LoggerManager;
import manager.CollectionManager;
import manager.CommandManager;

import java.io.*;
        import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
        import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Класс для управления runtime сервера.
 * ИЗМЕНЕНО: Добавлена многопоточная обработка запросов согласно заданию.
 */
public class ServerRuntimeManager {
    private final InetSocketAddress address;
    private Selector selector;
    private final CommandManager commandManager;
    private final CollectionManager collectionManager;
    private final Serializer serializer;
    // ИЗМЕНЕНО: Убран FileManager, так как хранение в БД
    private final LoggerManager logger = new LoggerManager(ServerRuntimeManager.class);

    // ДОБАВЛЕНО: Cached thread pool для обработки запросов согласно заданию
    private final ExecutorService requestProcessorPool = Executors.newCachedThreadPool();

    private Boolean isWorking = true;

    public ServerRuntimeManager(InetSocketAddress address,
                                CommandManager commandManager,
                                CollectionManager collectionManager,
                                Serializer serializer) {
        this.address = address;
        this.commandManager = commandManager;
        this.collectionManager = collectionManager;
        this.serializer = serializer;
    }

    public boolean init() {
        try {
            this.selector = Selector.open();
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.bind(address);
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            logger.info("Сервер запущен.");
            return true;
        } catch (IOException e) {
            logger.error("Ошибка инициализации: " + e.getMessage());
            return false;
        }
    }

    public void start() {
        startConsoleThread();
        while (isWorking) {
            try {
                if (selector.select(500) == 0) continue;
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();
                    if (!key.isValid()) continue;

                    if (key.isAcceptable()) acceptClient(key);
                    else if (key.isReadable()) handleClientRequest(key);
                }
            } catch (IOException e) {
                logger.error("Ошибка селектора: " + e.getMessage());
            }
        }
    }

    /**
     * ОБРАБОТКА ЗАПРОСА КЛИЕНТА.
     * ИЗМЕНЕНО: Многопоточное чтение в новом потоке (Thread), обработка в Cached thread pool.
     */
    private void handleClientRequest(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel();
        SessionContext ctx = (SessionContext) key.attachment();

        try {
            if (!ctx.headerRead) {
                if (client.read(ctx.headerBuffer) == -1) { closeClient(key); return; }
                if (ctx.headerBuffer.hasRemaining()) return;

                ctx.headerBuffer.flip();
                ctx.totalLength = ctx.headerBuffer.getInt();

                ctx.tempFile = Files.createTempFile("server_data_", ".tmp");
                ctx.fileChannel = FileChannel.open(ctx.tempFile,
                        java.nio.file.StandardOpenOption.WRITE,
                        java.nio.file.StandardOpenOption.READ);
                ctx.headerRead = true;
                logger.info("Reading object.");
            }

            ByteBuffer buffer = ByteBuffer.allocate(65536);
            int read = client.read(buffer);
            if (read == -1) { closeClient(key); return; }

            if (read > 0) {
                buffer.flip();
                ctx.fileChannel.write(buffer);
                ctx.bytesAccumulated += read;
            }

            if (ctx.bytesAccumulated < ctx.totalLength) return;

            ctx.fileChannel.position(0);

            // ДОБАВЛЕНО: Чтение запроса в НОВОМ ПОТОКЕ (java.lang.Thread) согласно заданию
            Thread readThread = new Thread(() -> {
                Request request;
                try (InputStream fis = Files.newInputStream(ctx.tempFile);
                     ObjectInputStream ois = new ObjectInputStream(fis)) {
                    request = (Request) ois.readObject();

                    // ДОБАВЛЕНО: После чтения - отправка на обработку в Cached thread pool
                    requestProcessorPool.submit(() -> {
                        try {
                            // ИЗМЕНЕНО: Добавлена передача username для авторизации
                            Response response = commandManager.executeCommand(
                                    request.getCommandName(),
                                    request.getArgs(),
                                    request.getWorker(),
                                    request.getUsername()
                            );
                            sendResponse(client, response);
                        } catch (IOException e) {
                            logger.error("Ошибка при отправке ответа: " + e.getMessage());
                        } finally {
                            ctx.cleanup();
                            // Закрываем соединение после обработки запроса
                            try { client.close(); } catch (IOException ignored) {}
                        }
                    });
                } catch (ClassNotFoundException | IOException e) {
                    logger.error("Ошибка при чтении запроса: " + e.getMessage());
                    ctx.cleanup();
                    try { client.close(); } catch (IOException ignored) {}
                }
            });
            readThread.start();

            // Возвращаем управление, чтобы не блокировать selector
            return;

        } catch (IOException e) {
            logger.error("Сбой при передаче: " + e.getMessage());
            closeClient(key);
        }
    }

    /**
     * ОТПРАВКА ОТВЕТА.
     * ДОБАВЛЕНО: Отправка ответа в новом потоке (java.lang.Thread) согласно заданию.
     */
    private void sendResponse(SocketChannel client, Response response) throws IOException {
        // ДОБАВЛЕНО: Создание нового потока для отправки ответа
        Thread responseThread = new Thread(() -> {
            try {
                byte[] data = serializer.serialize(response);
                ByteBuffer out = ByteBuffer.allocate(4 + data.length);
                out.putInt(data.length);
                out.put(data);
                out.flip();
                while (out.hasRemaining()) client.write(out);
            } catch (IOException e) {
                logger.error("Ошибка при отправке ответа: " + e.getMessage());
            }
        });
        responseThread.start();
    }

    private void acceptClient(SelectionKey key) throws IOException {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel client = ssc.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ, new SessionContext());
    }

    private void closeClient(SelectionKey key) {
        SessionContext ctx = (SessionContext) key.attachment();
        if (ctx != null) ctx.cleanup();
        try { key.channel().close(); key.cancel(); } catch (IOException ignored) {}
    }

    private void startConsoleThread() {
        Thread t = new Thread(() -> {
            Scanner s = new Scanner(System.in);
            while (isWorking) {
                if (s.hasNextLine()) {
                    String cmd = s.nextLine().trim();
                    if ("exit".equals(cmd)) {
                        logger.info("Shutting down the server!");
                        isWorking = false;
                        selector.wakeup();
                        requestProcessorPool.shutdown();
                    } else if ("save".equals(cmd)) {
                        logger.info("Saving to database (auto-saved)!");
                        // ИЗМЕНЕНО: Убрано сохранение в файл, данные сохраняются в БД автоматически
                    } else {
                        logger.info("Incorrect command! Use exit or save!");
                    }
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private static class SessionContext {
        ByteBuffer headerBuffer = ByteBuffer.allocate(4);
        boolean headerRead = false;
        long totalLength = 0;
        long bytesAccumulated = 0;
        Path tempFile;
        FileChannel fileChannel;

        void cleanup() {
            try {
                if (fileChannel != null) fileChannel.close();
                if (tempFile != null) Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {}
            headerBuffer.clear();
            headerRead = false;
            bytesAccumulated = 0;
        }
    }
}