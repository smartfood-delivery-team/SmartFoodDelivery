package com.smartfood.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    private final DatabaseManager databaseManager;

    public DatabaseInitializer(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void initialize() throws SQLException {

        String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY,
                    username TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    role TEXT NOT NULL CHECK (role IN ('CUSTOMER', 'MANAGER'))
                )
                """;
        String createRestaurantsTable = """
        CREATE TABLE IF NOT EXISTS restaurants (
            id INTEGER PRIMARY KEY,
            name TEXT NOT NULL UNIQUE,
            is_open INTEGER NOT NULL DEFAULT 1
                CHECK (is_open IN (0, 1)),
            delivery_zone TEXT NOT NULL DEFAULT '',
            minimum_order REAL NOT NULL DEFAULT 0
                CHECK (minimum_order >= 0)
        )
        """;
        String createMenuItemsTable = """
        CREATE TABLE IF NOT EXISTS menu_items (
            id INTEGER PRIMARY KEY,
            restaurant_id INTEGER NOT NULL,
            name TEXT NOT NULL,
            price REAL NOT NULL
                CHECK (price >= 0),
            available INTEGER NOT NULL DEFAULT 1
                CHECK (available IN (0, 1)),
            FOREIGN KEY (restaurant_id)
                REFERENCES restaurants(id)
        )
        """;
        try (
                Connection connection = databaseManager.getConnection();
                Statement statement = connection.createStatement()
        ) {
            statement.execute(createUsersTable);
            statement.execute(createRestaurantsTable);
            statement.execute(createMenuItemsTable);

        }

    }
}