package database;

import manager.CollectionManager;
import models.Worker;
import models.Coordinates;
import models.Organization;
import models.Position;
import models.Status;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;

public class WorkerDatabaseManager {
    private final DatabaseManager databaseManager;

    public WorkerDatabaseManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void initWorkerTable() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS workers (
                id INTEGER PRIMARY KEY DEFAULT nextval('workers_id_seq'),
                name TEXT NOT NULL,
                coordinates_x DOUBLE PRECISION NOT NULL,
                coordinates_y INTEGER NOT NULL,
                creation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                salary DOUBLE PRECISION,
                position TEXT,
                status TEXT NOT NULL,
                organization_annual_turnover DOUBLE PRECISION,
                organization_employees_count INTEGER,
                creator_username TEXT NOT NULL,
                FOREIGN KEY (creator_username) REFERENCES USERS (username)
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

        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createSequenceSQL);
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean addWorkerToDB(Worker worker, String username) {
        initWorkerTable();
        String insertSQL = """
            INSERT INTO workers (
                id, name, coordinates_x, coordinates_y,
                creation_date, salary, position, status,
                organization_annual_turnover, organization_employees_count,
                creator_username
            ) VALUES (DEFAULT, ?, ?, ?, DEFAULT, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setString(1, worker.getName());
            pstmt.setDouble(2, worker.getCoordinates().getX());
            pstmt.setInt(3, worker.getCoordinates().getY().intValue());

            if (worker.getSalary() != null) {
                pstmt.setDouble(4, worker.getSalary());
            } else {
                pstmt.setNull(4, Types.DOUBLE);
            }

            if (worker.getPosition() != null) {
                pstmt.setString(5, worker.getPosition().name());
            } else {
                pstmt.setNull(5, Types.VARCHAR);
            }

            pstmt.setString(6, worker.getStatus().name());

            if (worker.getOrganization().getAnnualTurnover() != null) {
                pstmt.setDouble(7, worker.getOrganization().getAnnualTurnover());
            } else {
                pstmt.setNull(7, Types.DOUBLE);
            }

            if (worker.getOrganization().getEmployeesCount() != null) {
                pstmt.setInt(8, worker.getOrganization().getEmployeesCount());
            } else {
                pstmt.setNull(8, Types.INTEGER);
            }

            pstmt.setString(9, username);

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

    public Collection<Worker> loadWorkersFromDB() {
        initWorkerTable();
        Collection<Worker> collection = Collections.synchronizedCollection(new ArrayDeque<>());
        String selectSQL = "SELECT * FROM workers ORDER BY id";

        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {

            while (rs.next()) {
                Worker worker = createWorkerFromResultSet(rs);
                collection.add(worker);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return collection;
    }

    public boolean removeWorkerFromDB(Long id) {
        initWorkerTable();
        String deleteSQL = "DELETE FROM workers WHERE id = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            pstmt.setLong(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean clearWorkersTable(String username) {
        initWorkerTable();
        String deleteSQL = "DELETE FROM workers WHERE creator_username = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean canUserModifyWorker(Long workerId, String username) {
        initWorkerTable();
        String selectSQL = "SELECT creator_username FROM workers WHERE id = ?";

        try (Connection conn = databaseManager.getConnection();
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

    private Worker createWorkerFromResultSet(ResultSet rs) throws SQLException {
        initWorkerTable();
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