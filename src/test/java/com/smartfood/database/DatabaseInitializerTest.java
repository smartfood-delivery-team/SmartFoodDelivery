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
                  AND name IN ('users', 'restaurants', 'menu_items')
                """)
        ) {
            int count = 0;

            while (result.next()) {
                count++;
            }

            assertEquals(3, count);
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
}