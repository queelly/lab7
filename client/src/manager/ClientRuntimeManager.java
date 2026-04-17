package manager;

import network.TCPClient;
import models.builders.WorkerBuilder;
import models.Worker;
import network.Request;
import network.Response;
import java.util.Arrays;
import java.util.Set;
import java.util.Scanner;

import static models.builders.RequestBuilder.createRequest;

public class ClientRuntimeManager {
    private final TCPClient client; // Твой новый неблокирующий клиент
    private final ScannerManager scannerManager = new ScannerManager();
    private final PrinterManager printerManager = new PrinterManager();
    private Boolean isWorking = true;

    // ДОБАВЛЕНО: поля для хранения учетных данных пользователя
    private String username;
    private String password;

    public ClientRuntimeManager(TCPClient client) {
        this.client = client;
    }

    /**
     * ДОБАВЛЕНО: Метод для авторизации пользователя при запуске клиента.
     * Запрашивает логин и пароль, которые будут передаваться с каждым запросом.
     * Также отправляет команду login на сервер для регистрации/авторизации.
     */
    private boolean authenticate() {
        System.out.println("=== Авторизация ===");
        while (true) {
            System.out.print("Введите логин: ");
            if (!scannerManager.hasNext()) return false;
            username = scannerManager.readLine().trim();

            System.out.print("Введите пароль: ");
            if (!scannerManager.hasNext()) return false;
            password = scannerManager.readLine().trim();

            if (!username.isEmpty() && !password.isEmpty()) {
                // ИЗМЕНЕНО: Отправляем команду login на сервер для проверки/регистрации
                Request loginRequest = createRequest("login", new String[]{password}, username, password);
                if (client.sendRequest(loginRequest)) {
                    Response response = client.receiveResponse();
                    if (response != null && response.isSuccess()) {
                        System.out.println(response.getMessage());
                        return true;
                    } else if (response != null) {
                        System.out.println("Ошибка: " + response.getMessage());
                        System.out.println("Попробуйте снова.");
                    } else {
                        System.out.println("Не удалось получить ответ от сервера. Попробуйте снова.");
                    }
                } else {
                    System.out.println("Не удалось отправить запрос. Попробуйте снова.");
                }
            } else {
                System.out.println("Логин и пароль не могут быть пустыми. Попробуйте снова.");
            }
        }
    }

    public void run() throws InterruptedException {
        // ДОБАВЛЕНО: Авторизация перед запуском основного цикла
        if (!authenticate()) {
            System.out.println("Не удалось пройти авторизацию. Клиент завершает работу.");
            return;
        }

        System.out.println("Клиент запущен. Введите команду (help для справки):");

        while (true) {
            System.out.print("> ");
            if (!scannerManager.hasNext()) break;

            String line = scannerManager.readLine().trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+");
            String commandName = parts[0];
            String[] args = (parts.length > 1) ? Arrays.copyOfRange(parts, 1, parts.length) : new String[]{};

            if (commandName.equals("exit")) {
                isWorking = false;
                break;
            } else if (commandName.equals("execute_script")) {
                // ДОБАВЛЕНО: Передача учетных данных в ScriptManager для авторизации
                new ScriptManager(client, isWorking, username, password).execute(args[0]);
                continue;
            }

            // ИЗМЕНЕНО: Создание запроса с передачей учетных данных пользователя
            Request request = createRequest(commandName, args, scannerManager, printerManager, username, password);
            if (request == null) continue;

            if (!client.sendRequest(request)) {
                printerManager.printErr("Не удалось отправить запрос на сервер.");
                while (!client.connect()) {
                    printerManager.printErr("Попытка подключения к серверу...");
                    Thread.sleep(10);
                }
                continue;
            }

            Response response = client.receiveResponse();

            if (response != null) {
                if (response.isSuccess()) {
                    printerManager.println(response.getMessage());
                } else {
                    printerManager.printErr("Сервер вернул ошибку: " + response.getMessage());
                }
            } else {
                printerManager.printErr("Ошибка: Ответ от сервера не получен или соединение разорвано.");
            }
        }
    }
}