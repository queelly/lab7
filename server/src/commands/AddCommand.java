package commands;

import manager.CollectionManager;
import models.Worker;
import network.Response;
import database.WorkerDatabaseManager;

/**
 * Команда add для добавления нового работника.
 * ИЗМЕНЕНО: Сначала добавление в БД, затем в коллекцию в памяти.
 * ДОБАВЛЕНО: Проверка авторизации пользователя.
 */
public class AddCommand implements Executable {

    private CollectionManager collection;

    public AddCommand(CollectionManager collection) {
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

        // ИЗМЕНЕНО: Сначала добавляем в БД, затем обновляем коллекцию в памяти
        if (WorkerDatabaseManager.addWorkerToDB(worker, username)) {
            // ДОБАВЛЕНО: Обновление коллекции в памяти только после успешного добавления в БД
            collection.addWithoutIdGeneration(worker);
            return new Response("New worker was added successfully!", true);
        } else {
            return new Response("New worker was not added(", false);
        }
    }

    // Переопределенный метод для обратной совместимости
    @Override
    public Response execute(String[] args, Worker worker) {
        return execute(args, worker, null);
    }

    @Override
    public String toString() {
        return ": add new Worker to Collection";
    }
}