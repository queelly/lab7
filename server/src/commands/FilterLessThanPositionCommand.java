package commands;

import manager.CollectionManager;
import models.Position;
import models.Worker;
import network.Response;

import java.util.ArrayDeque;
import java.util.List;

public class FilterLessThanPositionCommand implements Executable {

    private CollectionManager collection;

    public FilterLessThanPositionCommand(CollectionManager collection) {
        this.collection = collection;
    }

    @Override
    public Response execute(String[] args, Worker worker, String username) {
        // ДОБАВЛЕНО: Проверка авторизации
        if (username == null || username.isEmpty()) {
            return new Response("Ошибка: Пользователь не авторизован!", false);
        }

        if (args.length == 0) {
            return new Response("Missing args!", false);
        }
        if (args.length > 1) {
            return new Response("Too much args! Must be 1!", false);
        }
        Position position = null;
        for (Position element : Position.values()) {
            if (args[0].equalsIgnoreCase(element.name())) {
                position = element;
                break;
            }
        }
        if (position == null) {
            return new Response("Invalid Position value!", false);
        }
        ArrayDeque<Worker> filteredWorkers = collection.filterLessThanPosition(position);
        if (filteredWorkers.isEmpty()) {
            return new Response("Result is empty!", true);
        } else {
            return new Response("Filtered result:\n", filteredWorkers, true);
        }
    }

    // Переопределенный метод для обратной совместимости
    @Override
    public Response execute(String[] args, Worker worker) {
        return execute(args, worker, null);
    }

    @Override
    public String toString() {
        return ": print Workers whose Position is less than the specified one";
    }
}