import manager.ClientRuntimeManager;
import network.TCPClient;

public class MainClient {
    public static void main(String[] args) {
        String host = "localhost";
//        String host = "helios.cs.ifmo.ru";
        int port = 11113;
        TCPClient client = new TCPClient(host, port);
        while (!client.connect()) {}
        ClientRuntimeManager runtime = new ClientRuntimeManager(client);
        try {
            runtime.run();
        } catch (InterruptedException ignored) {
        }
        client.close();
    }
}