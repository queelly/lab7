import manager.ClientRuntimeManager;
import network.TCPClient;

public class MainClient {
    public static void main(String[] args) {
        String host = "localhost";
//         String host = "helios.cs.ifmo.ru";
        int port = 9999;

        TCPClient client = new TCPClient(host, port);

        System.out.println("Попытка подключения к серверу...");

        while (!client.connect()) {
            try {
                Thread.sleep(2000);
                System.out.println("Переподключение...");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        ClientRuntimeManager runtime = new ClientRuntimeManager(client);

        try {
            runtime.run();
        } catch (Exception e) {
            System.err.println("Критическая ошибка во время работы: " + e.getMessage());
        } finally {
            client.close();
            System.out.println("Работа клиента завершена.");
        }
    }
}