package commands;

import auth.UserManager;
import models.Worker;
import network.Response;

/**
* Команда login для авторизации/регистрации пользователя.
* ДОБАВЛЕНО: Новая команда согласно заданию для обработки входа пользователя.
*/
public class LoginCommand implements Executable {

    @Override
    public Response execute(String[] args, Worker worker, String username) {
        // ИЗМЕНЕНО: Проверка наличия учетных данных
        if (username == null || username.isEmpty()) {
            return new Response("Ошибка: Не передано имя пользователя!", false);
        }

        // Проверяем количество аргументов (должен быть 1 - пароль)
        if (args.length != 1) {
            return new Response("Использование: login <password>", false);
        }

        String password = args[0];

        // ДОБАВЛЕНО: Логика автоматической регистрации/авторизации
        if (UserManager.userExists(username)) {
                // Пользователь существует - проверяем пароль
            if (UserManager.authenticateUser(username, password)) {
                return new Response("Авторизация успешна! Добро пожаловать, " + username, true);
            } else {
                return new Response("Ошибка: Неверный пароль!", false);
            }
        } else {
            // Пользователь не существует - регистрируем автоматически
            if (UserManager.registerUser(username, password)) {
                return new Response("Пользователь " + username + " зарегистрирован и выполнен вход!", true);
            } else {
                return new Response("Ошибка регистрации пользователя!", false);
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
        return ": login to the system (automatic registration if user doesn't exist)";
    }
}
