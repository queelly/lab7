package manager;

import auth.UserManager;
import commands.LoginCommand;
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

public class ServerRuntimeManager {
    private final InetSocketAddress address;
    private Selector selector;
    private final CommandManager commandManager;
    private final CollectionManager collectionManager;
    private final Serializer serializer;
    private final LoggerManager logger = new LoggerManager(ServerRuntimeManager.class);

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

            key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);

            Thread readThread = new Thread(() -> {
                Request request;
                try (InputStream fis = Files.newInputStream(ctx.tempFile);
                     ObjectInputStream ois = new ObjectInputStream(fis)) {

                    request = (Request) ois.readObject();

                    requestProcessorPool.submit(() -> {
                        try {
                            Response response;
                            if (!request.getCommandName().trim().equals("login") && !UserManager.authenticateUser(request.getUsername(), request.getPassword())) {
                                response = new Response(
                                        "Ошибка аутентификации! Попробуйте войти еще раз!", false);
                            } else if (request.getCommandName().trim().equals("login") && request.getPassword() == null) {
                                response = new LoginCommand().execute(request.getArgs(), request.getWorker(), request.getUsername());
                            } else {
                                response = commandManager.executeCommand(
                                        request.getCommandName(),
                                        request.getArgs(),
                                        request.getWorker(),
                                        request.getUsername()
                                );
                            }
                            Thread sendThread = new Thread(() -> {
                                try {
                                    sendResponse(client, response);
                                } catch (IOException e) {
                                    logger.error("Ошибка при отправке ответа: " + e.getMessage());
                                } finally {
                                    // Закрываем соединение после успешной отправки
                                    ctx.cleanup();
                                    try { client.close(); key.cancel(); } catch (IOException ignored) {}
                                }
                            });
                            sendThread.start();

                        } catch (Exception e) {
                            logger.error("Ошибка при исполнении команды: " + e.getMessage());
                            ctx.cleanup();
                            try { client.close(); key.cancel(); } catch (IOException ignored) {}
                        }
                    });
                } catch (ClassNotFoundException | IOException e) {
                    logger.error("Ошибка при чтении запроса: " + e.getMessage());
                    ctx.cleanup();
                    try { client.close(); key.cancel(); } catch (IOException ignored) {}
                }
            });
            readThread.start();

        } catch (IOException e) {
            logger.error("Сбой при передаче: " + e.getMessage());
            closeClient(key);
        }
    }

    private void sendResponse(SocketChannel client, Response response) throws IOException {
        byte[] data = serializer.serialize(response);
        ByteBuffer out = ByteBuffer.allocate(4 + data.length);
        out.putInt(data.length);
        out.put(data);
        out.flip();

        while (out.hasRemaining()) {
            int bytesWritten = client.write(out);
            if (bytesWritten == 0) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
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
                    } else {
                        logger.info("Incorrect command! Use exit as existing command!");
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