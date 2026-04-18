package commands;

import manager.CollectionManager;
import models.Worker;
import network.Response;
import database.WorkerDatabaseManager;

public class RemoveByIdCommand implements Executable {

    private CollectionManager collection;

    public RemoveByIdCommand(CollectionManager collection) {
        this.collection = collection;
    }

    @Override
    public Response execute(String[] args, Worker worker, String username) {
        if (username == null || username.isEmpty()) {
            return new Response("Ошибка: Пользователь не авторизован!", false);
        }

        if (args.length == 0) {
            return new Response("Missing args!", false);
        }
        if (args.length > 1) {
            return new Response("Too much args! Must be 1!", false);
        }
        if (collection.getCollectionSize() == 0) {
            return new Response("Collection is empty!", false);
        } else {
            try {
                long id = Long.parseLong(args[0]);
                if (collection.findById(id) == null) {
                    return new Response("Worker with id " + id + " doesn't exist", false);
                }

                if (!WorkerDatabaseManager.canUserModifyWorker(id, username)) {
                    return new Response("Ошибка: У вас нет прав на удаление этого объекта!", false);
                }

                if (WorkerDatabaseManager.removeWorkerFromDB(id)) {
                    collection.removeById(id);
                    return new Response("Worker with id " + id + " was removed!", true);
                } else {
                    return new Response("Ошибка при удалении из БД", false);
                }
            } catch (NumberFormatException e) {
                return new Response("Wrong id format!", false);
            }
        }
    }

    // Переопределенный метод для обратной совместимости
    @Override
    public Response execute(String[] args, Worker worker) {
        return execute(args, worker, null);
    }

    @Override
    public String toString() {
        return ": remove Worker from the head of Collection";
    }
}