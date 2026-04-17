package database;

import manager.CollectionManager;
import models.Worker;
import models.Coordinates;
import models.Organization;
import models.Position;
import models.Status;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * Класс для управления коллекцией Worker в базе данных PostgreSQL.
 * ДОБАВЛЕНО: Новый класс для работы с БД согласно заданию.
 */
public class WorkerDatabaseManager {

    /**
     * Инициализация таблицы workers и последовательности для id.
     */
    public static void initWorkerTable() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS workers (
                id INTEGER PRIMARY KEY DEFAULT nextval('workers_id_seq'),
                name VARCHAR(255) NOT NULL,
                coordinates_x DOUBLE PRECISION NOT NULL,
                coordinates_y INTEGER NOT NULL,
                creation_date TIMESTAMP NOT NULL,
                salary DOUBLE PRECISION,
                position VARCHAR(255),
                status VARCHAR(255) NOT NULL,
                organization_annual_turnover DOUBLE PRECISION,
                organization_employees_count INTEGER,
                creator_username VARCHAR(255) NOT NULL
            )
            """;

        String createSequenceSQL = """
            CREATE SEQUENCE IF NOT EXISTS workers_id_seq
            START WITH 1
            INCREMENT BY 1
            NO MINVALUE
            NO MAXVALUE
            CACHE 1
            """;

        // ИЗМЕНЕНО: используем новый метод getConnection() без параметров для инициализации таблиц
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createSequenceSQL);
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Добавление работника в базу данных.
     * ДОБАВЛЕНО: Метод добавляет объект в БД и возвращает true при успехе.
     * @param worker работник для добавления
     * @param username имя пользователя, создавшего объект
     * @return true если добавление успешно
     */
    public static boolean addWorkerToDB(Worker worker, String username) {
        String insertSQL = """
            INSERT INTO workers (
                name, coordinates_x, coordinates_y, creation_date,
                salary, position, status,
                organization_annual_turnover, organization_employees_count,
                creator_username
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;

        // ИЗМЕНЕНО: используем новый метод getConnection() без параметров для подключения к БД
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setString(1, worker.getName());
            pstmt.setDouble(2, worker.getCoordinates().getX());
            pstmt.setInt(3, worker.getCoordinates().getY().intValue());
            pstmt.setTimestamp(4, Timestamp.valueOf(worker.getCreationDate()));

            if (worker.getSalary() != null) {
                pstmt.setDouble(5, worker.getSalary());
            } else {
                pstmt.setNull(5, Types.DOUBLE);
            }

            if (worker.getPosition() != null) {
                pstmt.setString(6, worker.getPosition().name());
            } else {
                pstmt.setNull(6, Types.VARCHAR);
            }

            pstmt.setString(7, worker.getStatus().name());

            if (worker.getOrganization().getAnnualTurnover() != null) {
                pstmt.setDouble(8, worker.getOrganization().getAnnualTurnover());
            } else {
                pstmt.setNull(8, Types.DOUBLE);
            }

            if (worker.getOrganization().getEmployeesCount() != null) {
                pstmt.setInt(9, worker.getOrganization().getEmployeesCount());
            } else {
                pstmt.setNull(9, Types.INTEGER);
            }

            pstmt.setString(10, username);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int generatedId = rs.getInt("id");
                worker.setId((long) generatedId);
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Загрузка всех работников из базы данных в память.
     * ДОБАВЛЕНО: Метод загружает коллекцию из БД при старте сервера.
     * @return CollectionManager с загруженными работниками
     */
    public static CollectionManager loadWorkersFromDB() {
        CollectionManager collectionManager = new CollectionManager();
        String selectSQL = "SELECT * FROM workers ORDER BY id";

        // ИЗМЕНЕНО: используем новый метод getConnection() без параметров для подключения к БД
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {

            while (rs.next()) {
                Worker worker = createWorkerFromResultSet(rs);
                collectionManager.addWithoutIdGeneration(worker);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return collectionManager;
    }

    /**
     * Удаление работника из базы данных по ID.
     * @param id ID работника
     * @return true если удаление успешно
     */
    public static boolean removeWorkerFromDB(Long id) {
        String deleteSQL = "DELETE FROM workers WHERE id = ?";

        // ИЗМЕНЕНО: используем новый метод getConnection() без параметров для подключения к БД
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            pstmt.setLong(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Очистка всей таблицы workers.
     * @return true если очистка успешна
     */
    public static boolean clearWorkersTable() {
        String deleteSQL = "DELETE FROM workers";

        // ИЗМЕНЕНО: используем новый метод getConnection() без параметров для подключения к БД
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(deleteSQL);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Проверка прав доступа: может ли пользователь модифицировать работника.
     * ДОБАВЛЕНО: Метод для проверки принадлежности объекта пользователю.
     * @param workerId ID работника
     * @param username имя пользователя
     * @return true если пользователь является создателем объекта
     */
    public static boolean canUserModifyWorker(Long workerId, String username) {
        String selectSQL = "SELECT creator_username FROM workers WHERE id = ?";

        // ИЗМЕНЕНО: используем новый метод getConnection() без параметров для подключения к БД
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            pstmt.setLong(1, workerId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String creatorUsername = rs.getString("creator_username");
                return creatorUsername.equals(username);
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Создание объекта Worker из ResultSet.
     */
    private static Worker createWorkerFromResultSet(ResultSet rs) throws SQLException {
        Long id = (long) rs.getInt("id");
        String name = rs.getString("name");
        Float x = rs.getFloat("coordinates_x");
        Integer y = rs.getInt("coordinates_y");
        Coordinates coordinates = new Coordinates(x, y.doubleValue());

        LocalDateTime creationDate = rs.getTimestamp("creation_date").toLocalDateTime();

        Double salary = rs.getDouble("salary");
        if (rs.wasNull()) {
            salary = null;
        }

        String positionStr = rs.getString("position");
        Position position = positionStr != null ? Position.valueOf(positionStr) : null;

        Status status = Status.valueOf(rs.getString("status"));

        Double annualTurnover = rs.getDouble("organization_annual_turnover");
        if (rs.wasNull()) {
            annualTurnover = null;
        }

        Integer employeesCount = rs.getInt("organization_employees_count");
        if (rs.wasNull()) {
            employeesCount = null;
        }

        Organization organization = new Organization(annualTurnover, employeesCount);

        return new Worker(id, name, coordinates, creationDate, salary, position, status, organization);
    }
}