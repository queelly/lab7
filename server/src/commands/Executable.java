package commands;

import models.Worker;
import network.Response;

/**
 * Интерфейс для выполнения команд.
 * ДОБАВЛЕНО: Метод execute с параметром username для авторизации.
 */
public interface Executable {
    // Основной метод выполнения команды с авторизацией
    Response execute(String[] args, Worker worker, String username);

    // Метод для обратной совместимости (может быть удален в будущем)
    default Response execute(String[] args, Worker worker) {
        return execute(args, worker, null);
    }
}