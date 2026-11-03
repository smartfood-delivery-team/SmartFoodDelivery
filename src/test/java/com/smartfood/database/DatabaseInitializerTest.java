package com.smartfood.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseInitializerTest {

    @TempDir
    Path tempDir;

    @Test
    void testTablesCreated() throws SQLException {

        Path dbPath = tempDir.resolve("test.db");

        DatabaseManager db = new DatabaseManager(
                "jdbc:sqlite:" + dbPath.toAbsolutePath()
        );

        DatabaseInitializer initializer = new DatabaseInitializer(db);

        initializer.initialize();

        try (
                Connection connection = db.getConnection();
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("""
        SELECT name
        FROM sqlite_master
        WHERE type = 'table'
          AND name IN (
              'users',
              'restaurants',
              'menu_items',
              'orders',
              'order_items',
              'drivers'
          )
        """)
        ) {
            int count = 0;

            while (result.next()) {
                count++;
            }

            assertEquals(6, count);
        }
    }

    @Test
    void testInitializeTwice() throws SQLException {

        Path dbPath = tempDir.resolve("test-twice.db");

        DatabaseManager db = new DatabaseManager(
                "jdbc:sqlite:" + dbPath.toAbsolutePath()
        );

        DatabaseInitializer initializer = new DatabaseInitializer(db);

        assertDoesNotThrow(() -> {
            initializer.initialize();
            initializer.initialize();
        });
    }

    @Test
    void testForeignKeyConstraint() throws SQLException {

        Path dbPath = tempDir.resolve("test-fk.db");

        DatabaseManager db = new DatabaseManager(
                "jdbc:sqlite:" + dbPath.toAbsolutePath()
        );

        new DatabaseInitializer(db).initialize();

        try (
                Connection connection = db.getConnection();
                Statement statement = connection.createStatement()
        ) {
            assertThrows(SQLException.class, () ->
                    statement.executeUpdate("""
                        INSERT INTO menu_items
                        (restaurant_id, name, price, available)
                        VALUES (999, 'Pizza', 25.0, 1)
                        """)
            );
        }
    }

    @Test
    void testCheckConstraints() throws SQLException {

        Path dbPath = tempDir.resolve("test-check.db");

        DatabaseManager db = new DatabaseManager(
                "jdbc:sqlite:" + dbPath.toAbsolutePath()
        );

        new DatabaseInitializer(db).initialize();

        try (
                Connection connection = db.getConnection();
                Statement statement = connection.createStatement()
        ) {
            statement.executeUpdate("""
                INSERT INTO restaurants
                (id, name, is_open, delivery_zone, minimum_order)
                VALUES (1, 'Pizza House', 1, 'Nablus', 30)
                """);

            assertThrows(SQLException.class, () ->
                    statement.executeUpdate("""
                        INSERT INTO menu_items
                        (restaurant_id, name, price, available)
                        VALUES (1, 'Pizza', -25.0, 1)
                        """)
            );
        }
    }
    @Test
    void testOrderForeignKeyConstraint() throws SQLException {

        Path dbPath = tempDir.resolve("test-order-fk.db");

        DatabaseManager db = new DatabaseManager(
                "jdbc:sqlite:" + dbPath.toAbsolutePath()
        );

        new DatabaseInitializer(db).initialize();

        try (
                Connection connection = db.getConnection();
                Statement statement = connection.createStatement()
        ) {

            assertThrows(SQLException.class, () ->
                    statement.executeUpdate("""
                    INSERT INTO orders (
                        id,
                        customer_username,
                        restaurant_id,
                        delivery_address,
                        status,
                        total
                    )
                    VALUES (
                        'ORD-1',
                        'missing-user',
                        999,
                        'Nablus',
                        'PENDING',
                        50.0
                    )
                    """)
            );
        }
    }
    @Test
    void testAssignedDriverColumnExists() throws SQLException {

        Path dbPath = tempDir.resolve("test-driver-column.db");

        DatabaseManager db = new DatabaseManager(
                "jdbc:sqlite:" + dbPath.toAbsolutePath()
        );

        new DatabaseInitializer(db).initialize();

        boolean columnExists = false;

        try (
                Connection connection = db.getConnection();
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(
                        "PRAGMA table_info(orders)"
                )
        ) {

            while (result.next()) {

                if ("assigned_driver_id".equals(
                        result.getString("name")
                )) {
                    columnExists = true;
                    break;
                }
            }
        }

        assertTrue(
                columnExists,
                "orders table must contain assigned_driver_id"
        );
    }
    @Test
    void testDriverAvailabilityConstraint() throws SQLException {

        Path dbPath = tempDir.resolve("test-driver-check.db");

        DatabaseManager db = new DatabaseManager(
                "jdbc:sqlite:" + dbPath.toAbsolutePath()
        );

        new DatabaseInitializer(db).initialize();

        try (
                Connection connection = db.getConnection();
                Statement statement = connection.createStatement()
        ) {

            assertThrows(SQLException.class, () ->
                    statement.executeUpdate("""
                        INSERT INTO drivers (
                            id,
                            name,
                            available,
                            distance,
                            estimated_time
                        )
                        VALUES (
                            'D1',
                            'Omar',
                            5,
                            2.5,
                            8
                        )
                        """)
            );
        }
    }
    @Test
    void testAssignedDriverForeignKeyConstraint() throws SQLException {

        Path dbPath = tempDir.resolve("test-assigned-driver-fk.db");

        DatabaseManager db = new DatabaseManager(
                "jdbc:sqlite:" + dbPath.toAbsolutePath()
        );

        new DatabaseInitializer(db).initialize();

        try (
                Connection connection = db.getConnection();
                Statement statement = connection.createStatement()
        ) {

            // Create a valid customer.
            statement.executeUpdate("""
                INSERT INTO users (
                    username,
                    password_hash,
                    role
                )
                VALUES (
                    'lana',
                    'test-hash',
                    'CUSTOMER'
                )
                """);

            // Create a valid restaurant.
            statement.executeUpdate("""
                INSERT INTO restaurants (
                    id,
                    name,
                    is_open,
                    delivery_zone,
                    minimum_order
                )
                VALUES (
                    1,
                    'Pizza House',
                    1,
                    'Nablus',
                    20
                )
                """);

            // Driver D999 does not exist.
            assertThrows(SQLException.class, () ->
                    statement.executeUpdate("""
                        INSERT INTO orders (
                            id,
                            customer_username,
                            restaurant_id,
                            delivery_address,
                            status,
                            total,
                            assigned_driver_id
                        )
                        VALUES (
                            'ORD-1',
                            'lana',
                            1,
                            'Main Street, Nablus',
                            'PENDING',
                            50,
                            'D999'
                        )
                        """)
            );
        }
    }
    @Test
    void testExistingOrdersTableIsMigrated() throws SQLException {

        Path dbPath = tempDir.resolve("test-migration.db");

        DatabaseManager db = new DatabaseManager(
                "jdbc:sqlite:" + dbPath.toAbsolutePath()
        );

        // Simulate the old Sprint 2 orders table.
        try (
                Connection connection = db.getConnection();
                Statement statement = connection.createStatement()
        ) {

            statement.execute("""
                CREATE TABLE orders (
                    id TEXT PRIMARY KEY,
                    customer_username TEXT NOT NULL,
                    restaurant_id INTEGER NOT NULL,
                    delivery_address TEXT NOT NULL,
                    status TEXT NOT NULL,
                    total REAL NOT NULL
                )
                """);

            statement.execute("""
                INSERT INTO orders (
                    id,
                    customer_username,
                    restaurant_id,
                    delivery_address,
                    status,
                    total
                )
                VALUES (
                    'ORD-OLD',
                    'lana',
                    1,
                    'Nablus',
                    'PENDING',
                    50
                )
                """);
        }

        // Run the updated initializer.
        DatabaseInitializer initializer =
                new DatabaseInitializer(db);

        assertDoesNotThrow(initializer::initialize);

        try (
                Connection connection = db.getConnection();
                Statement statement = connection.createStatement()
        ) {

            // Check that the existing order was preserved.
            try (ResultSet result = statement.executeQuery("""
                SELECT id
                FROM orders
                WHERE id = 'ORD-OLD'
                """)) {

                assertTrue(result.next());
                assertEquals("ORD-OLD", result.getString("id"));
            }

            // Check that migration added the new column.
            boolean found = false;

            try (ResultSet result = statement.executeQuery(
                    "PRAGMA table_info(orders)"
            )) {

                while (result.next()) {

                    if ("assigned_driver_id".equals(
                            result.getString("name")
                    )) {
                        found = true;
                        break;
                    }
                }
            }

            assertTrue(found);
        }
    }
}