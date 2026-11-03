package com.smartfood.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class DatabaseInitializer {

    private final DatabaseManager databaseManager;

    public DatabaseInitializer(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void initialize() throws SQLException {

        // ==========================================
        // SPRINT 1 - USERS
        // ==========================================

        String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY,
                    username TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    role TEXT NOT NULL
                        CHECK (role IN ('CUSTOMER', 'MANAGER'))
                )
                """;

        // ==========================================
        // SPRINT 1 - RESTAURANTS
        // ==========================================

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

        // ==========================================
        // SPRINT 1 - MENU ITEMS
        // ==========================================

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

        // ==========================================
        // SPRINT 2 - ORDERS
        // ==========================================

        String createOrdersTable = """
                CREATE TABLE IF NOT EXISTS orders (
                    id TEXT PRIMARY KEY,

                    customer_username TEXT NOT NULL,
                    restaurant_id INTEGER NOT NULL,

                    delivery_address TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'PENDING',

                    total REAL NOT NULL
                        CHECK (total >= 0),

                    FOREIGN KEY (customer_username)
                        REFERENCES users(username),

                    FOREIGN KEY (restaurant_id)
                        REFERENCES restaurants(id),

                    CHECK (
                        status IN (
                            'PENDING',
                            'CONFIRMED',
                            'PREPARING',
                            'READY',
                            'OUT_FOR_DELIVERY',
                            'DELIVERED',
                            'CANCELLED'
                        )
                    )
                )
                """;

        // ==========================================
        // SPRINT 2 - ORDER ITEMS
        // ==========================================

        String createOrderItemsTable = """
                CREATE TABLE IF NOT EXISTS order_items (
                    id INTEGER PRIMARY KEY,

                    order_id TEXT NOT NULL,
                    menu_item_id INTEGER NOT NULL,

                    quantity INTEGER NOT NULL
                        CHECK (quantity > 0),

                    unit_price REAL NOT NULL
                        CHECK (unit_price >= 0),

                    FOREIGN KEY (order_id)
                        REFERENCES orders(id),

                    FOREIGN KEY (menu_item_id)
                        REFERENCES menu_items(id)
                )
                """;
        String createDriversTable = """
        CREATE TABLE IF NOT EXISTS drivers (
            id TEXT PRIMARY KEY,

            name TEXT NOT NULL,

            available INTEGER NOT NULL DEFAULT 1
                CHECK (available IN (0, 1)),

            distance REAL NOT NULL
                CHECK (distance >= 0),

            estimated_time INTEGER NOT NULL
                CHECK (estimated_time >= 0)
        )
        """;
        // ==========================================
        // EXECUTE TABLE CREATION
        // ==========================================

        try (
                Connection connection = databaseManager.getConnection();
                Statement statement = connection.createStatement()
        ) {

            statement.execute(createUsersTable);
            statement.execute(createRestaurantsTable);
            statement.execute(createMenuItemsTable);

            statement.execute(createOrdersTable);
            statement.execute(createOrderItemsTable);
            statement.execute(createDriversTable);


// Check whether the orders table already has the column.
            boolean hasAssignedDriverColumn = false;

            try (ResultSet columns =
                         statement.executeQuery("PRAGMA table_info(orders)")) {

                while (columns.next()) {

                    if ("assigned_driver_id".equals(
                            columns.getString("name")
                    )) {
                        hasAssignedDriverColumn = true;
                        break;
                    }
                }
            }

// Add the column only when it does not exist.
            if (!hasAssignedDriverColumn) {

                statement.execute("""
            ALTER TABLE orders
            ADD COLUMN assigned_driver_id TEXT
            REFERENCES drivers(id)
            """);
            }
        }
    }

}