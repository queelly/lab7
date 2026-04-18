package network;

import network.Request;
import network.Response;
import network.Serializer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class TCPClient {
    private final String host;
    private final int port;
    private SocketChannel clientChannel;
    private Selector selector;
    private final Serializer serializer = new Serializer();

    private final int RECONNECT_DELAY = 3000;

    public TCPClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean connect() {
        try {
            if (selector != null) selector.close();
            if (clientChannel != null) clientChannel.close();

            this.selector = Selector.open();
            this.clientChannel = SocketChannel.open();
            this.clientChannel.configureBlocking(false);
            this.clientChannel.connect(new InetSocketAddress(host, port));
            this.clientChannel.register(selector, SelectionKey.OP_CONNECT);

            if (waitConnection()) {
                return true;
            }

        } catch (IOException e) {
            try { Thread.sleep(RECONNECT_DELAY); } catch (InterruptedException ignored) {}
        }
        System.out.println("Не удалось подключиться к серверу");
        return false;
    }

    private boolean waitConnection() throws IOException {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 5000) {
            if (selector.select(1000) == 0) continue;
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();
                if (key.isConnectable()) {
                    if (clientChannel.finishConnect()) {
                        clientChannel.register(selector, SelectionKey.OP_READ);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean sendRequest(Request request) {
        try {
            if (!connect()) {
                System.out.println("Не удалось установить соединение для отправки запроса.");
                return false;
            }

            byte[] data = serializer.serialize(request);
            if (data == null) return false;

            ByteBuffer buffer = ByteBuffer.allocate(4 + data.length);
            buffer.putInt(data.length);
            buffer.put(data);
            buffer.flip();

            while (buffer.hasRemaining()) {
                if (clientChannel.write(buffer) == 0 && buffer.hasRemaining()) {
                    Thread.sleep(10);
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("Ошибка при отправке: " + e.getMessage());
            return false;
        }
    }


    public Response receiveResponse() {
        try {
            ByteBuffer header = ByteBuffer.allocate(4);
            if (!readFully(header)) return null;
            header.flip();
            int len = header.getInt();

            ByteBuffer data = ByteBuffer.allocate(len);
            if (!readFully(data)) return null;

            return serializer.deserialize(data.array(), Response.class);

        } catch (IOException e) {
            System.out.println("Связь с сервером потеряна: " + e.getMessage());
            return null;
        }
    }

    private boolean readFully(ByteBuffer buffer) throws IOException {
        long start = System.currentTimeMillis();
        while (buffer.hasRemaining()) {
            int read = clientChannel.read(buffer);
            if (read == -1) throw new IOException("Сервер закрыл соединение");

            if (read == 0) {
                if (System.currentTimeMillis() - start > 10000) throw new IOException("Таймаут чтения ответа");
                try { Thread.sleep(10); } catch (InterruptedException ignored) {}
            }
        }
        return true;
    }

    public void close() {
        try {
            if (clientChannel != null) clientChannel.close();
            if (selector != null) selector.close();
        } catch (IOException ignored) {}
    }
}