package com.smartfood.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initializes and updates the SQLite database schema.
 *
 * Sprint 1: Users, restaurants, and menu items.
 * Sprint 2: Orders and order items.
 * Sprint 3: Drivers and driver assignments.
 * Sprint 4: Delivery fees, pricing strategies, and payments.
 *
 * Existing tables and records are preserved.
 */
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
        // SPRINT 3 - DRIVERS
        // ==========================================

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
        // SPRINT 2, 3, 4 - ORDERS
        // ==========================================

        String createOrdersTable = """
                CREATE TABLE IF NOT EXISTS orders (
                    id TEXT PRIMARY KEY,

                    customer_username TEXT NOT NULL,

                    restaurant_id INTEGER NOT NULL,

                    delivery_address TEXT NOT NULL,

                    status TEXT NOT NULL DEFAULT 'PENDING'
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
                        ),

                    total REAL NOT NULL
                        CHECK (total >= 0),

                    assigned_driver_id TEXT,

                    delivery_distance_km REAL NOT NULL DEFAULT 0
                        CHECK (delivery_distance_km >= 0),

                    delivery_fee REAL NOT NULL DEFAULT 0
                        CHECK (delivery_fee >= 0),

                    pricing_strategy TEXT NOT NULL DEFAULT 'NORMAL'
                        CHECK (
                            pricing_strategy IN (
                                'NORMAL',
                                'EXPRESS',
                                'PEAK_HOUR',
                                'PROMOTIONAL'
                            )
                        ),

                    payment_status TEXT NOT NULL DEFAULT 'UNPAID'
                        CHECK (
                            payment_status IN (
                                'UNPAID',
                                'PAID',
                                'FAILED'
                            )
                        ),

                    FOREIGN KEY (customer_username)
                        REFERENCES users(username),

                    FOREIGN KEY (restaurant_id)
                        REFERENCES restaurants(id),

                    FOREIGN KEY (assigned_driver_id)
                        REFERENCES drivers(id)
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

        // ==========================================
        // EXECUTE TABLE CREATION
        // ==========================================

        try (
                Connection connection = databaseManager.getConnection();
                Statement statement = connection.createStatement()
        ) {

            // Sprint 1
            statement.execute(createUsersTable);
            statement.execute(createRestaurantsTable);
            statement.execute(createMenuItemsTable);

            // Sprint 3
            statement.execute(createDriversTable);

            // Sprint 2 and Sprint 4
            statement.execute(createOrdersTable);
            statement.execute(createOrderItemsTable);

            // ==========================================
            // DATABASE MIGRATIONS
            // ==========================================

            // Sprint 3: Assigned driver
            addColumnIfMissing(
                    connection,
                    "orders",
                    "assigned_driver_id",
                    """
                    assigned_driver_id TEXT
                    REFERENCES drivers(id)
                    """
            );

            // Sprint 4: Delivery distance
            addColumnIfMissing(
                    connection,
                    "orders",
                    "delivery_distance_km",
                    """
                    delivery_distance_km REAL NOT NULL DEFAULT 0
                    CHECK (delivery_distance_km >= 0)
                    """
            );

            // Sprint 4: Delivery fee
            addColumnIfMissing(
                    connection,
                    "orders",
                    "delivery_fee",
                    """
                    delivery_fee REAL NOT NULL DEFAULT 0
                    CHECK (delivery_fee >= 0)
                    """
            );

            // Sprint 4: Pricing strategy
            addColumnIfMissing(
                    connection,
                    "orders",
                    "pricing_strategy",
                    """
                    pricing_strategy TEXT NOT NULL DEFAULT 'NORMAL'
                    CHECK (
                        pricing_strategy IN (
                            'NORMAL',
                            'EXPRESS',
                            'PEAK_HOUR',
                            'PROMOTIONAL'
                        )
                    )
                    """
            );

            // Sprint 4: Payment status
            addColumnIfMissing(
                    connection,
                    "orders",
                    "payment_status",
                    """
                    payment_status TEXT NOT NULL DEFAULT 'UNPAID'
                    CHECK (
                        payment_status IN (
                            'UNPAID',
                            'PAID',
                            'FAILED'
                        )
                    )
                    """
            );
        }
    }

    // =====================================================
    // HELPER METHOD: ADD COLUMN ONLY IF MISSING
    // =====================================================

    /**
     * Adds a database column if it does not already exist.
     *
     * This allows older Sprint 2 and Sprint 3 databases
     * to be upgraded without deleting existing orders.
     *
     * @param connection active database connection
     * @param tableName database table name
     * @param columnName column to check
     * @param columnDefinition SQL definition of the column
     * @throws SQLException if the database operation fails
     */
    private void addColumnIfMissing(
            Connection connection,
            String tableName,
            String columnName,
            String columnDefinition
    ) throws SQLException {

        boolean columnExists = false;

        String checkSql =
                "PRAGMA table_info(" + tableName + ")";

        try (
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(checkSql)
        ) {

            while (result.next()) {

                if (columnName.equals(
                        result.getString("name")
                )) {

                    columnExists = true;
                    break;
                }
            }
        }

        if (!columnExists) {

            String alterSql =
                    "ALTER TABLE " + tableName
                            + " ADD COLUMN "
                            + columnDefinition;

            try (
                    Statement statement = connection.createStatement()
            ) {

                statement.execute(alterSql);
            }
        }
    }
}