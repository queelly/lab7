package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Properties;

public class DatabaseManager {
    private static final String DB_HOST = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "localhost";
    private static final String DB_NAME = "studs";
    private static final String DB_URL = "jdbc:postgresql://" + DB_HOST + ":5432/" + DB_NAME;

    public static final String DEFAULT_USER = System.getenv("DB_USER") != null ?
            System.getenv("DB_USER") : "queelly";
    public static final String DEFAULT_PASSWORD = System.getenv("DB_PASSWORD") != null ?
            System.getenv("DB_PASSWORD") : "";

//    private static Connection connection;

    public Connection getConnection() throws SQLException {
        return getConnection(DEFAULT_USER, DEFAULT_PASSWORD);
    }

    private static Connection getConnection(String username, String password) throws SQLException {
        Connection connection;
        Properties props = new Properties();
        props.setProperty("user", username);
        props.setProperty("password", password);
        connection = DriverManager.getConnection(DB_URL, props);
        connection.setAutoCommit(true);
        return connection;
    }
}