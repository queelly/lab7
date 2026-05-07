package commands;

import auth.UserManager;
import manager.CollectionManager;
import models.Worker;
import network.Response;
import database.WorkerDatabaseManager;

/**
 * Команда add_if_max для добавления работника, если он больше максимального.
 * ИЗМЕНЕНО: Добавлена проверка авторизации и работа с БД.
 */
public class AddIfMaxCommand implements Executable {
    private CollectionManager collection;

    public AddIfMaxCommand(CollectionManager collection) {
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
                            max(java.util.Comparator.naturalOrder()).get()) > 0) {
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
                "in case the value of Worker is more than maximal value in Collection";
    }
}