package com.smartfood.repository;

import com.smartfood.database.DatabaseManager;
import com.smartfood.domain.Driver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite implementation of DriverStore.
 *
 * Stores driver information and availability
 * in the drivers database table.
 */
public class SqliteDriverRepository implements DriverStore {

    private final DatabaseManager databaseManager;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public SqliteDriverRepository(
            DatabaseManager databaseManager
    ) {

        if (databaseManager == null) {
            throw new IllegalArgumentException(
                    "Database manager cannot be null"
            );
        }

        this.databaseManager = databaseManager;
    }

    // =====================================================
    // SAVE DRIVER
    // =====================================================

    @Override
    public void save(Driver driver) {

        if (driver == null) {
            throw new IllegalArgumentException(
                    "Driver cannot be null"
            );
        }

        String sql = """
                INSERT INTO drivers (
                    id,
                    name,
                    available,
                    distance,
                    estimated_time
                )
                VALUES (?, ?, ?, ?, ?)

                ON CONFLICT(id) DO UPDATE SET
                    name = excluded.name,
                    available = excluded.available,
                    distance = excluded.distance,
                    estimated_time = excluded.estimated_time
                """;

        try (
                Connection connection = databaseManager.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, driver.getId());

            statement.setString(2, driver.getName());

            statement.setInt(
                    3,
                    driver.isAvailable() ? 1 : 0
            );

            statement.setDouble(
                    4,
                    driver.getDistance()
            );

            statement.setInt(
                    5,
                    driver.getEstimatedTime()
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Failed to save driver: "
                            + driver.getId(),
                    e
            );
        }
    }

    // =====================================================
    // FIND DRIVER BY ID
    // =====================================================

    @Override
    public Driver findById(String id) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Driver ID cannot be empty"
            );
        }

        String sql = """
                SELECT
                    id,
                    name,
                    available,
                    distance,
                    estimated_time
                FROM drivers
                WHERE id = ?
                """;

        try (
                Connection connection = databaseManager.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, id);

            try (ResultSet result = statement.executeQuery()) {

                if (!result.next()) {
                    return null;
                }

                return mapResultSetToDriver(result);
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Failed to retrieve driver: " + id,
                    e
            );
        }
    }

    // =====================================================
    // FIND ALL DRIVERS
    // =====================================================

    @Override
    public List<Driver> findAll() {

        String sql = """
                SELECT
                    id,
                    name,
                    available,
                    distance,
                    estimated_time
                FROM drivers
                ORDER BY id
                """;

        List<Driver> drivers = new ArrayList<>();

        try (
                Connection connection = databaseManager.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet result = statement.executeQuery()
        ) {

            while (result.next()) {

                Driver driver =
                        mapResultSetToDriver(result);

                drivers.add(driver);
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Failed to retrieve drivers",
                    e
            );
        }

        return drivers;
    }

    // =====================================================
    // FIND AVAILABLE DRIVERS
    // =====================================================

    @Override
    public List<Driver> findAvailableDrivers() {

        String sql = """
                SELECT
                    id,
                    name,
                    available,
                    distance,
                    estimated_time
                FROM drivers
                WHERE available = 1
                ORDER BY id
                """;

        List<Driver> availableDrivers = new ArrayList<>();

        try (
                Connection connection = databaseManager.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet result = statement.executeQuery()
        ) {

            while (result.next()) {

                Driver driver =
                        mapResultSetToDriver(result);

                availableDrivers.add(driver);
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Failed to retrieve available drivers",
                    e
            );
        }

        return availableDrivers;
    }

    // =====================================================
    // CONVERT RESULTSET INTO DRIVER OBJECT
    // =====================================================

    private Driver mapResultSetToDriver(ResultSet result)
            throws SQLException {

        String id = result.getString("id");

        String name = result.getString("name");

        boolean available =
                result.getInt("available") == 1;

        double distance =
                result.getDouble("distance");

        int estimatedTime =
                result.getInt("estimated_time");

        return new Driver(
                id,
                name,
                available,
                distance,
                estimatedTime
        );
    }
}