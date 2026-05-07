package commands;

import auth.UserManager;
import manager.CollectionManager;
import models.Worker;
import network.Response;
import database.WorkerDatabaseManager;

public class AddCommand implements Executable {

    private CollectionManager collection;

    public AddCommand(CollectionManager collection) {
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

        if (workerDatabaseManager.addWorkerToDB(worker, username)) {
            collection.addWithoutIdGeneration(worker);
            return new Response("New worker was added successfully!", true);
        } else {
            return new Response("New worker was not added(", false);
        }
    }

    @Override
    public String toString() {
        return ": add new Worker to Collection";
    }
}