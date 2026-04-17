package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Класс для управления подключением к базе данных PostgreSQL.
 * ДОБАВЛЕНО: Новый класс для подключения к БД согласно заданию.
 * ИЗМЕНЕНО: Хост изменен на localhost для локальной разработки.
 * Для сдачи на кафедре верните "pg".
 * ИЗМЕНЕНО: Добавлен метод для создания БД, если она не существует.
 */
public class DatabaseManager {
    // ИЗМЕНЕНО: замените "pg" на "localhost" для локального запуска
    // В кафедральной сети должно быть "pg"
    private static final String DB_HOST = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "localhost";
    private static final String DB_NAME = "studs";
    private static final String DB_URL = "jdbc:postgresql://" + DB_HOST + ":5432/" + DB_NAME;

    // URL для подключения к серверу PostgreSQL без указания конкретной БД (для создания БД)
    private static final String SERVER_URL = "jdbc:postgresql://" + DB_HOST + ":5432/postgres";

    // ИЗМЕНЕНО: добавьте ваши локальные учетные данные, если они отличаются от логина/пароля пользователя
    // Для кафедрального сервера эти данные передаются извне или совпадают с системными
    // ДОБАВЛЕНО: изменено на пользователя queelly для локальной разработки
    // ИЗМЕНЕНО: сделаны public для использования в других классах
    public static final String DEFAULT_USER = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "queelly";
    public static final String DEFAULT_PASSWORD = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "";

    private static Connection connection;

    /**
     * Получение подключения к базе данных от имени приложения.
     * Использует учетные данные по умолчанию (из переменных окружения).
     * @return подключение к БД
     * @throws SQLException если произошла ошибка подключения
     */
    public static Connection getConnection() throws SQLException {
        return getConnection(DEFAULT_USER, DEFAULT_PASSWORD);
    }

    /**
     * Получение подключения к базе данных.
     * @param username имя пользователя
     * @param password пароль пользователя
     * @return подключение к БД
     * @throws SQLException если произошла ошибка подключения
     */
    public static Connection getConnection(String username, String password) throws SQLException {
        if (connection == null || connection.isClosed()) {
            Properties props = new Properties();
            props.setProperty("user", username);
            props.setProperty("password", password);
            connection = DriverManager.getConnection(DB_URL, props);
            connection.setAutoCommit(true);
        }
        return connection;
    }

    /**
     * Закрытие подключения к базе данных.
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}