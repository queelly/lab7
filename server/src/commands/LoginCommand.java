package commands;

import auth.UserManager;
import database.WorkerDatabaseManager;
import models.Worker;
import network.Response;

public class LoginCommand implements Executable {

    @Override
    public Response execute(String[] args, Worker worker, String username, WorkerDatabaseManager workerDatabaseManager,
                            UserManager userManager) {
        userManager.initUserTable();
        if (username == null || username.isEmpty()) {
            return new Response("Ошибка: Не передано имя пользователя!", false);
        }

        String password = args[0];

        if (userManager.userExists(username)) {
            if (userManager.authenticateUser(username, password)) {
                return new Response("Авторизация успешна! Добро пожаловать, " + username, true);
            } else {
                return new Response("Ошибка: Неверный пароль!", false);
            }
        } else {
            if (userManager.registerUser(username, password)) {
                return new Response("Пользователь " + username + " зарегистрирован и выполнен вход!", true);
            } else {
                return new Response("Ошибка регистрации пользователя!", false);
            }
        }
    }

    @Override
    public String toString() {
        return ": login to the system (automatic registration if user doesn't exist)";
    }
}
