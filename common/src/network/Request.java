package network;

import manager.ScannerManager;
import models.Worker;

import java.io.Serializable;
import java.util.Set;

/**
 * Класс запроса от клиента к серверу.
 * ДОБАВЛЕНО: Поля username и password для авторизации согласно заданию.
 */
public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    // ДОБАВЛЕНО: поля для авторизации пользователя
    private final String username;
    private final String password;

    private final String commandName;
    private final String[] args;
    private final Worker worker;

    public Request(String commandName, String[] args, Worker worker) {
        this(commandName, args, worker, null, null);
    }

    /**
     * ДОБАВЛЕНО: Конструктор с параметрами авторизации.
     */
    public Request(String commandName, String[] args, Worker worker, String username, String password) {
        this.commandName = commandName;
        this.args = args;
        this.worker = worker;
        this.username = username;
        this.password = password;
    }

    public Request(String commandName, String[] args) {
        this(commandName, args, null, null, null);
    }

    public String getCommandName() {
        return commandName;
    }

    public String[] getArgs() {
        return args;
    }

    public Worker getWorker() {
        return worker;
    }

    /**
     * ДОБАВЛЕНО: Геттеры для полей авторизации.
     */
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}