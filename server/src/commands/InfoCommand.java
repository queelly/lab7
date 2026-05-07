package commands;

import auth.UserManager;
import database.WorkerDatabaseManager;
import manager.CollectionManager;
import models.Worker;
import network.Response;

public class InfoCommand implements Executable {

    private CollectionManager collection;

    public InfoCommand(CollectionManager collection) {
        this.collection = collection;
    }

    @Override
    public Response execute(String[] args, Worker worker, String username, WorkerDatabaseManager workerDatabaseManager,
                            UserManager userManager) {
        if (username == null || username.isEmpty()) {
            return new Response("Ошибка: Пользователь не авторизован!", false);
        }

        if (args.length != 0) {
            return new Response("Command does not accept args!", false);
        }
        String message = "Initialization time: " + collection.getInitializationDateTime() + "\n" +
                "Collection type: " + collection.getCollectionType() + "\n" +
                "Current Collection size: " + collection.getCollectionSize() + "\n" +
                "Collection's elements:" + "\n" +
                collection.getCollectionAsString();
        return new Response(message, true);
    }

    @Override
    public String toString() {
        return ": print all info about collection";
    }
}