package manager;

import commands.Executable;
import models.Worker;
import network.Response;

import java.util.HashMap;

public class CommandManager {
    private HashMap<String, Executable> commands = new HashMap<>();

    public void addCommand(String commandName, Executable command) {
        commands.put(commandName, command);
    }

    public HashMap<String, Executable> getCommands() {
        return this.commands;
    }

    public Response executeCommand(String commandName, String[] args, Worker worker, String username) {
        Executable command = commands.get(commandName);
        if (command == null) {
            return new Response("That command doesn't exist!", false);
        } else {
            return command.execute(args, worker, username);
        }
    }
}