package commands;

import manager.CollectionManager;
import models.Worker;
import network.Response;
import database.WorkerDatabaseManager;

public class ClearCommand implements Executable {

    private CollectionManager collection;

    public ClearCommand(CollectionManager collection) {
        this.collection = collection;
    }

    @Override
    public Response execute(String[] args, Worker worker, String username) {
        if (username == null || username.isEmpty()) {
            return new Response("Ошибка: Пользователь не авторизован!", false);
        }

        if (args.length != 0) {
            return new Response("Command does not accept args!", false);
        }

        if (WorkerDatabaseManager.clearWorkersTable(username)) {
            collection.setCollection(WorkerDatabaseManager.loadWorkersFromDB());
            return new Response("Collection was cleared successfully!", true);
        } else {
            return new Response("Collection was not cleared(", false);
        }
    }

    @Override
    public Response execute(String[] args, Worker worker) {
        return execute(args, worker, null);
    }

    @Override
    public String toString() {
        return ": clear Collection";
    }
}