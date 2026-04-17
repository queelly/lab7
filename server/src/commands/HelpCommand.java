package commands;

import manager.CommandManager;
import models.Worker;
import network.Response;

public class HelpCommand implements Executable {

    private CommandManager commandManager;

    public HelpCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public Response execute(String[] args, Worker worker, String username) {
        // ДОБАВЛЕНО: Проверка авторизации
        if (username == null || username.isEmpty()) {
            return new Response("Ошибка: Пользователь не авторизован!", false);
        }

        if (args.length != 0) {
            return new Response("Command does not accept args!", false);
        }
        StringBuilder message = new StringBuilder();
        message.append("Here are all available commands:\n");
        commandManager.getCommands().forEach(
                (commandName, command) ->
                        message.append(commandName + command + "\n")
        );
        return new Response(message.toString(), true);
    }

    // Переопределенный метод для обратной совместимости
    @Override
    public Response execute(String[] args, Worker worker) {
        return execute(args, worker, null);
    }

    @Override
    public String toString() {
        return ": show information about all commands";
    }
}