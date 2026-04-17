package manager;

import commands.Executable;
import models.Worker;
import network.Response;

import java.util.HashMap;

/**
 * Класс для управления командами.
 * ИЗМЕНЕНО: Добавлена поддержка авторизации пользователя.
 */
public class CommandManager {
    private HashMap<String, Executable> commands = new HashMap<>();

    public void addCommand(String commandName, Executable command) {
        commands.put(commandName, command);
    }

    public HashMap<String, Executable> getCommands() {
        return this.commands;
    }

    /**
     * ДОБАВЛЕНО: Метод выполнения команды с авторизацией пользователя.
     * @param commandName имя команды
     * @param args аргументы команды
     * @param worker объект Worker (если требуется)
     * @param username имя пользователя
     * @return Response с результатом выполнения
     */
    public Response executeCommand(String commandName, String[] args, Worker worker, String username) {
        Executable command = commands.get(commandName);
        if (command == null) {
            return new Response("That command doesn't exist!", false);
        } else {
            // ИЗМЕНЕНО: Вызов метода execute с параметром username
            return command.execute(args, worker, username);
        }
    }

    // Перегруженный метод для обратной совместимости
    public Response executeCommand(String commandName, String[] args, Worker worker) {
        return executeCommand(commandName, args, worker, null);
    }
}