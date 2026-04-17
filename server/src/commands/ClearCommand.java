package commands;

import manager.CollectionManager;
import models.Worker;
import network.Response;
import database.WorkerDatabaseManager;

/**
 * Команда clear для очистки коллекции.
 * ИЗМЕНЕНО: Добавлена очистка БД и проверка авторизации.
 * ДОБАВЛЕНО: Проверка авторизации пользователя.
 */
public class ClearCommand implements Executable {

    private CollectionManager collection;

    public ClearCommand(CollectionManager collection) {
        this.collection = collection;
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

        // ИЗМЕНЕНО: Очищаем и БД, и коллекцию в памяти
        if (WorkerDatabaseManager.clearWorkersTable()) {
            collection.clear();
            return new Response("Collection was cleared successfully!", true);
        } else {
            return new Response("Collection was not cleared(", false);
        }
    }

    // Переопределенный метод для обратной совместимости
    @Override
    public Response execute(String[] args, Worker worker) {
        return execute(args, worker, null);
    }

    @Override
    public String toString() {
        return ": clear Collection";
    }
}