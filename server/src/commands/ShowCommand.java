package commands;

import manager.CollectionManager;
import models.Worker;
import network.Response;

/**
 * Команда show для отображения коллекции.
 * ДОБАВЛЕНО: Проверка авторизации пользователя.
 */
public class ShowCommand implements Executable {

    private CollectionManager collection;

    public ShowCommand(CollectionManager collection) {
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
        return new Response(collection.getCollectionAsString(), true);
    }

    // Переопределенный метод для обратной совместимости
    @Override
    public Response execute(String[] args, Worker worker) {
        return execute(args, worker, null);
    }

    @Override
    public String toString() {
        return ": show all Collection's elements";
    }
}