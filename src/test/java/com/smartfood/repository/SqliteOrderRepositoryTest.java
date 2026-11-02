package com.smartfood.repository;

import com.smartfood.database.DatabaseInitializer;
import com.smartfood.database.DatabaseManager;
import com.smartfood.domain.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SqliteOrderRepositoryTest {

    @TempDir
    Path tempDir;

    private Path dbPath;

    private DatabaseManager databaseManager;
    private SqliteOrderRepository repository;

    private User customer;
    private Restaurant restaurant;
    private MenuItem pizza;
    private MenuItem burger;

    // =====================================================
    // TEST SETUP
    // =====================================================

    @BeforeEach
    void setUp() throws SQLException {

        dbPath = tempDir.resolve("orders-test.db");

        databaseManager = new DatabaseManager(
                "jdbc:sqlite:" + dbPath.toAbsolutePath()
        );

        new DatabaseInitializer(databaseManager).initialize();

        repository = new SqliteOrderRepository(databaseManager);

        customer = new User(
                "lana",
                "test-password-hash",
                Role.CUSTOMER
        );

        restaurant = new Restaurant(
                "Pizza House",
                true,
                "Nablus",
                20.0
        );

        pizza = new MenuItem("Margherita", 25.0, true);
        burger = new MenuItem("Classic Burger", 30.0, true);

        restaurant.addMenuItem(pizza);
        restaurant.addMenuItem(burger);

        seedDatabase();
    }

    // =====================================================
    // INSERT TEST DATA INTO SQLITE
    // =====================================================

    private void seedDatabase() throws SQLException {

        String insertUser = """
                INSERT INTO users (
                    username,
                    password_hash,
                    role
                )
                VALUES (?, ?, ?)
                """;

        String insertRestaurant = """
                INSERT INTO restaurants (
                    id,
                    name,
                    is_open,
                    delivery_zone,
                    minimum_order
                )
                VALUES (1, ?, 1, ?, ?)
                """;

        String insertItem = """
                INSERT INTO menu_items (
                    restaurant_id,
                    name,
                    price,
                    available
                )
                VALUES (1, ?, ?, ?)
                """;

        try (Connection connection = databaseManager.getConnection()) {

            // Insert customer
            try (PreparedStatement statement =
                         connection.prepareStatement(insertUser)) {

                statement.setString(1, customer.getUsername());
                statement.setString(2, customer.getPasswordHash());
                statement.setString(3, customer.getRole().name());

                statement.executeUpdate();
            }

            // Insert restaurant
            try (PreparedStatement statement =
                         connection.prepareStatement(insertRestaurant)) {

                statement.setString(1, restaurant.getName());
                statement.setString(2, restaurant.getDeliveryZone());
                statement.setDouble(3, restaurant.getMinimumOrder());

                statement.executeUpdate();
            }

            // Insert menu items
            try (PreparedStatement statement =
                         connection.prepareStatement(insertItem)) {

                statement.setString(1, pizza.getName());
                statement.setDouble(2, pizza.getPrice());
                statement.setInt(3, 1);

                statement.executeUpdate();

                statement.setString(1, burger.getName());
                statement.setDouble(2, burger.getPrice());
                statement.setInt(3, 1);

                statement.executeUpdate();
            }
        }
    }

    // =====================================================
    // HELPER METHOD: CREATE ORDER
    // =====================================================

    private Order createOrder(String id) {

        return new Order(
                id,
                customer,
                restaurant,
                List.of(
                        new OrderItem(pizza, 2),
                        new OrderItem(burger, 1)
                ),
                "Main Street, Nablus"
        );
    }

    // =====================================================
    // TEST 1: SAVE AND RETRIEVE ORDER
    // =====================================================

    @Test
    void saveAndRetrieveOrder() {

        Order order = createOrder("ORD-100");

        repository.save(order);

        Order retrieved = repository.findById("ORD-100");

        assertNotNull(retrieved);

        assertEquals("ORD-100", retrieved.getId());

        assertEquals(
                "lana",
                retrieved.getCustomer().getUsername()
        );

        assertEquals(
                "Pizza House",
                retrieved.getRestaurant().getName()
        );

        assertEquals(
                OrderStatus.PENDING,
                retrieved.getStatus()
        );

        assertEquals(
                80.0,
                retrieved.getTotal(),
                0.001
        );

        assertEquals(2, retrieved.getItems().size());

        assertEquals(
                "Main Street, Nablus",
                retrieved.getDeliveryAddress()
        );
    }

    // =====================================================
    // TEST 2: FIND NONEXISTING ORDER
    // =====================================================

    @Test
    void findNonExistingOrderReturnsNull() {

        Order retrieved = repository.findById("ORD-999");

        assertNull(retrieved);
    }

    // =====================================================
    // TEST 3: FIND ALL ORDERS
    // =====================================================

    @Test
    void findAllReturnsSavedOrders() {

        Order firstOrder = createOrder("ORD-100");
        Order secondOrder = createOrder("ORD-101");

        repository.save(firstOrder);
        repository.save(secondOrder);

        List<Order> orders = repository.findAll();

        assertEquals(2, orders.size());

        assertTrue(
                orders.stream()
                        .anyMatch(order ->
                                order.getId().equals("ORD-100"))
        );

        assertTrue(
                orders.stream()
                        .anyMatch(order ->
                                order.getId().equals("ORD-101"))
        );
    }

    // =====================================================
    // TEST 4: UPDATE STATUS
    // =====================================================

    @Test
    void updatedStatusIsPersisted() {

        Order order = createOrder("ORD-100");

        repository.save(order);

        // Cancel the order
        order.setStatus(OrderStatus.CANCELLED);

        // Save updated status
        repository.save(order);

        // Retrieve from SQLite
        Order retrieved = repository.findById("ORD-100");

        assertNotNull(retrieved);

        assertEquals(
                OrderStatus.CANCELLED,
                retrieved.getStatus()
        );

        // Updating status should not remove order items
        assertEquals(2, retrieved.getItems().size());

        assertEquals(
                80.0,
                retrieved.getTotal(),
                0.001
        );
    }

    // =====================================================
    // TEST 5: DATA SURVIVES RECONNECTION
    // =====================================================

    @Test
    void ordersSurviveReconnection() {

        Order order = createOrder("ORD-100");

        repository.save(order);

        // Create a completely new database manager
        DatabaseManager secondDatabaseManager =
                new DatabaseManager(
                        "jdbc:sqlite:" + dbPath.toAbsolutePath()
                );

        // Create another repository instance
        SqliteOrderRepository secondRepository =
                new SqliteOrderRepository(secondDatabaseManager);

        Order retrieved =
                secondRepository.findById("ORD-100");

        assertNotNull(retrieved);

        assertEquals("ORD-100", retrieved.getId());

        assertEquals(
                "lana",
                retrieved.getCustomer().getUsername()
        );

        assertEquals(
                OrderStatus.PENDING,
                retrieved.getStatus()
        );

        assertEquals(
                80.0,
                retrieved.getTotal(),
                0.001
        );

        assertEquals(2, retrieved.getItems().size());
    }

    // =====================================================
    // TEST 6: UNAVAILABLE ITEM REJECTED
    // =====================================================

    @Test
    void unavailableItemIsRejected() throws SQLException {

        // Insert an unavailable item into SQLite
        String insertSql = """
                INSERT INTO menu_items (
                    restaurant_id,
                    name,
                    price,
                    available
                )
                VALUES (1, 'Unavailable Pizza', 35.0, 0)
                """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(insertSql)) {

            statement.executeUpdate();
        }

        // Create the same unavailable item in Java
        MenuItem unavailablePizza =
                new MenuItem("Unavailable Pizza", 35.0, false);

        Order order = new Order(
                "ORD-UNAVAILABLE",
                customer,
                restaurant,
                List.of(
                        new OrderItem(unavailablePizza, 1)
                ),
                "Main Street, Nablus"
        );

        // Repository should reject unavailable items
        assertThrows(
                IllegalArgumentException.class,
                () -> repository.save(order)
        );

        // Transaction should roll back
        assertNull(
                repository.findById("ORD-UNAVAILABLE")
        );
    }

    // =====================================================
    // TEST 7: INVALID RESTAURANT
    // =====================================================

    @Test
    void invalidRestaurantRollsBack() {

        Restaurant unknownRestaurant =
                new Restaurant(
                        "Unknown Restaurant",
                        true,
                        "Nablus",
                        0.0
                );

        Order order = new Order(
                "ORD-INVALID",
                customer,
                unknownRestaurant,
                List.of(
                        new OrderItem(pizza, 1)
                ),
                "Main Street, Nablus"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.save(order)
        );

        // The order must not be saved
        assertNull(
                repository.findById("ORD-INVALID")
        );
    }

    // =====================================================
    // TEST 8: HISTORICAL PRICES PRESERVED
    // =====================================================

    @Test
    void historicalItemPricesArePreserved() throws SQLException {

        Order order = createOrder("ORD-100");

        repository.save(order);

        // Original total:
        // 2 pizzas x 25 = 50
        // 1 burger x 30 = 30
        // Total = 80

        assertEquals(80.0, order.getTotal(), 0.001);

        // Change current menu price in SQLite
        String updatePriceSql = """
                UPDATE menu_items
                SET price = 40.0
                WHERE restaurant_id = 1
                  AND name = 'Margherita'
                """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(updatePriceSql)) {

            statement.executeUpdate();
        }

        // Retrieve the historical order
        Order retrieved = repository.findById("ORD-100");

        assertNotNull(retrieved);

        // Historical total must remain unchanged
        assertEquals(
                80.0,
                retrieved.getTotal(),
                0.001
        );

        // Confirm historical pizza price is still 25
        OrderItem retrievedPizza = retrieved.getItems()
                .stream()
                .filter(item ->
                        item.getMenuItem()
                                .getName()
                                .equals("Margherita"))
                .findFirst()
                .orElseThrow();

        assertEquals(
                25.0,
                retrievedPizza.getMenuItem().getPrice(),
                0.001
        );
    }

    // =====================================================
    // TEST 9: NO DUPLICATE ORDER ITEMS
    // =====================================================

    @Test
    void savingExistingOrderDoesNotDuplicateItems()
            throws SQLException {

        Order order = createOrder("ORD-100");

        // First save
        repository.save(order);

        // Save the same order again
        repository.save(order);

        String countSql = """
                SELECT COUNT(*)
                FROM order_items
                WHERE order_id = ?
                """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(countSql)) {

            statement.setString(1, "ORD-100");

            try (ResultSet result = statement.executeQuery()) {

                assertTrue(result.next());

                // Two items should still exist, not four
                assertEquals(2, result.getInt(1));
            }
        }
    }

    // =====================================================
    // TEST 10: PRICE MANIPULATION REJECTED
    // =====================================================

    @Test
    void manipulatedMenuItemPriceIsRejected() {

        // Database price is 25 NIS
        // Customer attempts to submit price = 1 NIS

        MenuItem manipulatedPizza =
                new MenuItem("Margherita", 1.0, true);

        Order order = new Order(
                "ORD-MANIPULATED",
                customer,
                restaurant,
                List.of(
                        new OrderItem(manipulatedPizza, 2)
                ),
                "Main Street, Nablus"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.save(order)
        );

        assertNull(
                repository.findById("ORD-MANIPULATED")
        );
    }

    // =====================================================
    // TEST 11: INVALID MENU ITEM
    // =====================================================

    @Test
    void menuItemFromAnotherRestaurantIsRejected() {

        MenuItem unknownItem =
                new MenuItem("Unknown Pizza", 50.0, true);

        Order order = new Order(
                "ORD-UNKNOWN-ITEM",
                customer,
                restaurant,
                List.of(
                        new OrderItem(unknownItem, 1)
                ),
                "Main Street, Nablus"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.save(order)
        );

        assertNull(
                repository.findById("ORD-UNKNOWN-ITEM")
        );
    }

    // =====================================================
    // TEST 12: ATOMIC TRANSACTION ROLLBACK
    // =====================================================

    @Test
    void failedSecondItemRollsBackEntireOrder() {

        MenuItem unknownItem =
                new MenuItem("Unknown Pizza", 50.0, true);

        Order order = new Order(
                "ORD-ROLLBACK",
                customer,
                restaurant,
                List.of(
                        new OrderItem(pizza, 1),
                        new OrderItem(unknownItem, 1)
                ),
                "Main Street, Nablus"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.save(order)
        );


        assertNull(
                repository.findById("ORD-ROLLBACK")
        );
    }
}