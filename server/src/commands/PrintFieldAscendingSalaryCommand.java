package commands;

import manager.CollectionManager;
import models.Worker;
import network.Response;

import java.util.List;

public class PrintFieldAscendingSalaryCommand implements Executable {

    private CollectionManager collection;

    public PrintFieldAscendingSalaryCommand(CollectionManager collection) {
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
        List<Double> sortedSalaries = collection.getSalariesAscending();
        if (sortedSalaries.isEmpty()) {
            return new Response("Result is empty!", true);
        } else {
            StringBuilder message = new StringBuilder();
            message.append("Sorted result:\n");
            for (Double salary : sortedSalaries) {
                message.append(salary).append("\n");
            }
            return new Response(message.toString(), true);
        }
    }

    @Override
    public Response execute(String[] args, Worker worker) {
        return execute(args, worker, null);
    }

    @Override
    public String toString() {
        return ": print Salaries of Workers in ascending order";
    }
}