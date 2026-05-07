package commands;

import auth.UserManager;
import database.WorkerDatabaseManager;
import manager.CollectionManager;
import models.Worker;
import network.Response;

import java.util.ArrayDeque;
import java.util.List;

public class FilterStartsWithNameCommand implements Executable {

    private CollectionManager collection;

    public FilterStartsWithNameCommand(CollectionManager collection) {
        this.collection = collection;
    }

    @Override
    public Response execute(String[] args, Worker worker, String username, WorkerDatabaseManager workerDatabaseManager,
                            UserManager userManager) {
        if (username == null || username.isEmpty()) {
            return new Response("Ошибка: Пользователь не авторизован!", false);
        }

        if (args.length == 0) {
            return new Response("Missing args!", false);
        }
        if (args.length > 1) {
            return new Response("Too much args! Must be 1!", false);
        }
        String name = args[0];
        ArrayDeque<Worker> filteredWorkers = collection.filterStartsWithName(name);
        if (filteredWorkers.isEmpty()) {
            return new Response("Result is empty!", true);
        } else {
            StringBuilder message = new StringBuilder();
            message.append("Filtered result:\n");
            for (Worker worker1 : filteredWorkers) {
                message.append(worker1).append("\n");
            }
            return new Response(message.toString(), true);
        }
    }

    @Override
    public String toString() {
        return ": print Workers whose Name starts with specified one";
    }
}