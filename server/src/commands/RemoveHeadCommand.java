package commands;

import manager.CollectionManager;
import models.Worker;
import network.Response;
import database.WorkerDatabaseManager;

public class RemoveHeadCommand implements Executable {

    private CollectionManager collection;

    public RemoveHeadCommand(CollectionManager collection) {
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
        if (collection.getCollectionSize() == 0) {
            return new Response("Collection is empty!", false);
        } else {
            synchronized (collection.getCollection()) {
                Worker headWorker = collection.getCollection().stream().min(java.util.Comparator.comparingLong(Worker::getId)).orElse(null);
                if (headWorker == null) {
                    return new Response("Collection is empty!", false);
                }

                if (!WorkerDatabaseManager.canUserModifyWorker(headWorker.getId(), username)) {
                    return new Response("Ошибка: У вас нет прав на удаление этого объекта!", false);
                }

                if (WorkerDatabaseManager.removeWorkerFromDB(headWorker.getId())) {
                    collection.removeById(headWorker.getId());
                    return new Response("Removed element:\n" + headWorker, true);
                } else {
                    return new Response("Ошибка при удалении из БД", false);
                }
            }
        }
    }

    @Override
    public Response execute(String[] args, Worker worker) {
        return execute(args, worker, null);
    }

    @Override
    public String toString() {
        return ": remove Worker from the head of Collection";
    }
}
