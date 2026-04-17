package manager;

import models.Worker;
import models.builders.WorkerBuilder;
import network.Request;
import network.Response;
import network.TCPClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

import static models.builders.RequestBuilder.createRequest;

public class ScriptManager {
    private final TCPClient client;
    private final Stack<String> scriptStack = new Stack<>();
    private final PrintStream printStream = new PrintStream(OutputStream.nullOutputStream());
    private final PrinterManager printerManager = new PrinterManager(
            printStream, printStream
    );
    private Boolean isWorking;

    // ДОБАВЛЕНО: поля для хранения учетных данных пользователя
    private final String username;
    private final String password;

    public ScriptManager(TCPClient client, Boolean isWorking) {
        this.client = client;
        this.isWorking = isWorking;
        this.username = null;
        this.password = null;
    }

    /**
     * ДОБАВЛЕНО: Конструктор с параметрами авторизации для передачи учетных данных
     * при выполнении скрипта.
     */
    public ScriptManager(TCPClient client, Boolean isWorking, String username, String password) {
        this.client = client;
        this.isWorking = isWorking;
        this.username = username;
        this.password = password;
    }

    public void execute(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.canRead()) {
            System.err.println("The file was not found or unavailable: " + filePath);
            return;
        }

        if (scriptStack.contains(file.getAbsolutePath())) {
            System.err.println("Recursion occurred! " + filePath);
            return;
        }

        scriptStack.push(file.getAbsolutePath());

        try (Scanner scriptScanner = new Scanner(file)) {
            while (scriptScanner.hasNextLine()) {
                String line = scriptScanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+");
                String cmd = parts[0].toLowerCase();
                String[] args = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[]{};
                if (cmd.equals("execute_script") && args.length == 1) {
                    execute(args[0]);
                } else {
                    handleScriptCommand(cmd, args, scriptScanner);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("The file was not found: " + e.getMessage());
        } finally {
            scriptStack.pop();
        }
    }

    private void handleScriptCommand(String cmd, String[] args, Scanner scanner) {
        // ИЗМЕНЕНО: Создание запроса с передачей учетных данных пользователя
        Request request = createRequest(cmd, args, new ScannerManager(scanner), printerManager, username, password);
        if (request == null) return;
        try {
            if (!client.sendRequest(request)) {
                System.err.println("Ошибка: Не удалось отправить запрос на сервер.");
                while (!client.connect()) {
                    printerManager.printErr("Попытка подключения к серверу...");
                    Thread.sleep(10);
                }
                printerManager.println("Подключение восстановлено!");
            }
            return;
        } catch (InterruptedException ignored) {
        }

        Response response = client.receiveResponse();

        if (response != null) {
            if (response.isSuccess()) {
                System.out.println(response.getMessage());
            } else {
                System.err.println("Сервер вернул ошибку: " + response.getMessage());
            }
        } else {
            System.err.println("Ошибка: Ответ от сервера не получен или соединение разорвано.");
        }
    }
}