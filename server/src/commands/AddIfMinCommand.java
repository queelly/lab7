package commands;

import auth.UserManager;
import manager.CollectionManager;
import models.Worker;
import network.Response;
import database.WorkerDatabaseManager;

public class AddIfMinCommand implements Executable {

    CollectionManager collection;

    public AddIfMinCommand(CollectionManager collection) {
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

        synchronized (collection.getCollection()) {
            if (collection.getCollectionSize() == 0 ||
                    worker.compareTo(collection.getCollection().stream().
                            min(java.util.Comparator.naturalOrder()).get()) < 0) {
                if (workerDatabaseManager.addWorkerToDB(worker, username)) {
                    collection.addWithoutIdGeneration(worker);
                    return new Response("New worker was added successfully!", true);
                } else {
                    return new Response("New worker was not added(", false);
                }
            } else {
                return new Response("New worker was not added(", false);
            }
        }
    }
    @Override
    public String toString() {
        return ": add new Worker to Collection " +
                "in case the value of Worker is less than minimal value in Collection";
    }
}