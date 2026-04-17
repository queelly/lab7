package models.builders;

import manager.PrinterManager;
import manager.ScannerManager;
import models.Worker;
import network.Request;
import network.TCPClient;

import java.util.Scanner;
import java.util.Set;

public class RequestBuilder {
    /**
     * ИЗМЕНЕНО: Добавлены параметры username и password для передачи учетных данных
     * с каждым запросом согласно заданию.
     */
    public static Request createRequest(String cmd, String[] args,
                                        ScannerManager scannerManager,
                                        PrinterManager printerManager,
                                        String username, String password) {
        Worker worker = null;
        Set<String> objectCommands = Set.of("add", "update", "add_if_max", "add_if_min");

        if (objectCommands.contains(cmd)) {
            worker = new WorkerBuilder(scannerManager, printerManager).build();
            if (worker == null) {
                System.err.println("Ошибка при создании Worker. Команда отменена.");
                return null;
            }
        }

        // ИЗМЕНЕНО: Создание запроса с передачей учетных данных пользователя
        return new Request(cmd, args, worker, username, password);
    }

    /**
     * ДОБАВЛЕНО: Перегруженный метод для создания запроса без Worker (для команды login).
     */
    public static Request createRequest(String cmd, String[] args,
                                        String username, String password) {
        // ИЗМЕНЕНО: Создание запроса без Worker с передачей учетных данных пользователя
        return new Request(cmd, args, null, username, password);
    }
}