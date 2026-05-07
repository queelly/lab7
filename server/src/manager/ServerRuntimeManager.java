package manager;

import auth.UserManager;
import commands.LoginCommand;
import database.DatabaseManager;
import database.WorkerDatabaseManager;
import network.Request;
import network.Response;
import network.Serializer;

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
    private final WorkerDatabaseManager workerDatabaseManager;
    private final DatabaseManager databaseManager;
    private final UserManager userManager;

    private final ExecutorService requestProcessorPool = Executors.newCachedThreadPool();
    private Boolean isWorking = true;

    public ServerRuntimeManager(InetSocketAddress address,
                                CommandManager commandManager,
                                CollectionManager collectionManager,
                                Serializer serializer,
                                WorkerDatabaseManager workerDatabaseManager,
                                DatabaseManager databaseManager,
                                UserManager userManager) {
        this.address = address;
        this.commandManager = commandManager;
        this.collectionManager = collectionManager;
        this.serializer = serializer;
        this.databaseManager = databaseManager;
        this.workerDatabaseManager = workerDatabaseManager;
        this.userManager = userManager;
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

    private void acceptClient(SelectionKey key) throws IOException {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel client = ssc.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ, new SessionContext());
    }

    private void handleClientRequest(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel();
        SessionContext ctx = (SessionContext) key.attachment();

        key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);

        Thread readThread = new Thread(() -> readDataAndProcess(key, client, ctx));
        readThread.start();
    }

    private void readDataAndProcess(SelectionKey key, SocketChannel client, SessionContext ctx) {
        try {
            if (!ctx.headerRead) {
                int read = client.read(ctx.headerBuffer);
                if (read == -1) { closeConnection(key); return; }

                if (ctx.headerBuffer.hasRemaining()) {
                    resumeReading(key);
                    return;
                }

                ctx.headerBuffer.flip();
                ctx.totalLength = ctx.headerBuffer.getInt();

                ctx.tempFile = Files.createTempFile("server_data_", ".tmp");
                ctx.fileChannel = FileChannel.open(ctx.tempFile,
                        java.nio.file.StandardOpenOption.WRITE,
                        java.nio.file.StandardOpenOption.READ);
                ctx.headerRead = true;
                logger.info("Чтение объекта размером: " + ctx.totalLength + " байт во временный файл.");
            }

            ByteBuffer buffer = ByteBuffer.allocate(65536);
            int read = client.read(buffer);
            if (read == -1) { closeConnection(key); return; }

            if (read > 0) {
                buffer.flip();
                ctx.fileChannel.write(buffer);
                ctx.bytesAccumulated += read;
            }

            if (ctx.bytesAccumulated < ctx.totalLength) {
                resumeReading(key);
                return;
            }

            ctx.fileChannel.position(0);
            Request request = deserializeRequest(ctx);

            requestProcessorPool.submit(() -> processRequest(key, client, request));

        } catch (IOException | ClassNotFoundException e) {
            logger.error("Сбой при передаче или десериализации: " + e.getMessage());
            closeConnection(key);
        }
    }

    private Request deserializeRequest(SessionContext ctx) throws IOException, ClassNotFoundException {
        try (InputStream fis = Files.newInputStream(ctx.tempFile);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (Request) ois.readObject();
        }
    }

    private void processRequest(SelectionKey key, SocketChannel client, Request request) {
        try {
            Response response = executeCommandLogic(request);

            Thread sendThread = new Thread(() -> sendResponse(key, client, response));
            sendThread.start();
        } catch (Exception e) {
            logger.error("Ошибка при исполнении команды: " + e.getMessage());
            closeConnection(key);
        }
    }

    private Response executeCommandLogic(Request request) {
        String commandName = request.getCommandName().trim();
        boolean isAuthenticated = userManager.authenticateUser(request.getUsername(), request.getPassword());

        if (!commandName.equals("login") && !isAuthenticated) {
            return new Response("Ошибка аутентификации! Попробуйте войти еще раз!", false);
        }

        if (commandName.equals("login") && request.getPassword() == null) {
            return new LoginCommand().execute(request.getArgs(), request.getWorker(), request.getUsername(),
                    workerDatabaseManager, userManager);
        }

        return commandManager.executeCommand(
                commandName,
                request.getArgs(),
                request.getWorker(),
                request.getUsername(),
                workerDatabaseManager,
                userManager
        );
    }

    private void sendResponse(SelectionKey key, SocketChannel client, Response response) {
        try {
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
        } catch (IOException e) {
            logger.error("Ошибка при отправке ответа: " + e.getMessage());
        } finally {
            closeConnection(key);
        }
    }

    private void resumeReading(SelectionKey key) {
        if (key.isValid()) {
            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
            selector.wakeup();
        }
    }

    private void closeConnection(SelectionKey key) {
        SessionContext ctx = (SessionContext) key.attachment();
        if (ctx != null) ctx.cleanup();
        try {
            key.channel().close();
            key.cancel();
        } catch (IOException ignored) {}
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