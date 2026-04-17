package auth;

import database.DatabaseManager;
import java.sql.*;

/**
 * Класс для управления пользователями в базе данных.
 * ДОБАВЛЕНО: Новый класс для регистрации и авторизации пользователей согласно заданию.
 */
public class UserManager {

    /**
     * Инициализация таблицы пользователей в БД (создание, если не существует).
     * ИЗМЕНЕНО: Добавлено поле salt для хранения соли пользователя.
     */
    public static void initUserTable() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS users (
                id SERIAL PRIMARY KEY,
                username VARCHAR(255) UNIQUE NOT NULL,
                password_hash VARCHAR(255) NOT NULL,
                salt VARCHAR(255) NOT NULL
            )
            """;

        // ИЗМЕНЕНО: используем новый метод getConnection() без параметров для инициализации таблиц
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            // Таблица уже существует или другая ошибка
            e.printStackTrace();
        }
    }

    /**
     * Регистрация нового пользователя.
     * @param username имя пользователя
     * @param password пароль пользователя
     * @return true если регистрация успешна, false если пользователь уже существует
     */
    public static boolean registerUser(String username, String password) {
        String insertSQL = "INSERT INTO users (username, password_hash, salt) VALUES (?, ?, ?)";
        String salt = PasswordHasher.generateSalt();
        String hashedPassword = PasswordHasher.hashPassword(password, salt);

        // ИЗМЕНЕНО: используем новый метод getConnection() без параметров для инициализации таблиц
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, salt);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            // Пользователь уже существует (нарушение уникальности)
            return false;
        }
    }

    /**
     * Авторизация пользователя.
     * @param username имя пользователя
     * @param password пароль пользователя
     * @return true если авторизация успешна, false иначе
     */
    public static boolean authenticateUser(String username, String password) {
        String selectSQL = "SELECT password_hash, salt FROM users WHERE username = ?";

        // ИЗМЕНЕНО: используем новый метод getConnection() без параметров для инициализации таблиц
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String salt = rs.getString("salt");
                String inputHash = PasswordHasher.hashPassword(password, salt);
                return storedHash.equals(inputHash);
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Проверка существования пользователя.
     * @param username имя пользователя
     * @return true если пользователь существует, false иначе
     */
    public static boolean userExists(String username) {
        String selectSQL = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Получение ID пользователя по имени.
     * @param username имя пользователя
     * @return ID пользователя или null если пользователь не найден
     */
    public static Integer getUserIdByUsername(String username) {
        String selectSQL = "SELECT id FROM users WHERE username = ?";

        // ИЗМЕНЕНО: используем новый метод getConnection() без параметров для инициализации таблиц
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}