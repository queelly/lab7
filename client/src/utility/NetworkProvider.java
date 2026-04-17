package utility;

import network.Request;
import network.Response;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NetworkProvider {
    private final String host;
    private final int port;
    private SocketChannel channel;
    private boolean working = true;

    public NetworkProvider(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() {
        if (channel != null && channel.isOpen() && channel.isConnected()) return;

        while (working) {
            try {
                channel = SocketChannel.open();
                channel.configureBlocking(false);
                channel.connect(new InetSocketAddress(host, port));

                System.out.println("Attempting to establish a connection with the server...");

                while (!channel.finishConnect()) {
                    Thread.sleep(1000);
                }

                System.out.println("The connection has been successfully established!");
                break;
            } catch (IOException | InterruptedException e) {
                System.out.println("The server is temporarily unavailable. Please try again in 10 seconds");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    public Response sendAndReceive(Request request) throws Exception {
        connect();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(request);
        oos.close();
        ByteBuffer writeBuffer = ByteBuffer.wrap(baos.toByteArray());
        while (writeBuffer.hasRemaining()) {
            channel.write(writeBuffer);
        }
        ByteBuffer readBuffer = ByteBuffer.allocate(16384);
        int bytesRead;
        while ((bytesRead = channel.read(readBuffer)) == 0) {
            Thread.sleep(10);
        }
        if (bytesRead == -1) throw new IOException("The server has closed the connection");
        readBuffer.flip();
        byte[] data = new byte[readBuffer.remaining()];
        readBuffer.get(data);

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        return (Response) ois.readObject();
    }
}