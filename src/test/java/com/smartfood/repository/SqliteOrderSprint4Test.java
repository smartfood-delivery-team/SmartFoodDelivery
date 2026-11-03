package com.smartfood.repository;

import com.smartfood.database.DatabaseInitializer;
import com.smartfood.database.DatabaseManager;

import com.smartfood.domain.MenuItem;
import com.smartfood.domain.Order;
import com.smartfood.domain.OrderItem;
import com.smartfood.domain.PaymentStatus;
import com.smartfood.domain.Restaurant;
import com.smartfood.domain.Role;
import com.smartfood.domain.User;

import com.smartfood.pricing.ExpressPricingStrategy;
import com.smartfood.pricing.NormalPricingStrategy;
import com.smartfood.pricing.PeakHourPricingStrategy;
import com.smartfood.pricing.PromotionalPricingStrategy;

import com.smartfood.service.PaymentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Sprint 4 SQLite persistence.
 *
 * Tests:
 * - Delivery fee persistence
 * - Dynamic pricing strategy persistence
 * - Payment status persistence
 * - Database reconnection
 * - Combined Sprint 4 functionality
 */
public class SqliteOrderSprint4Test {

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

        dbPath = tempDir.resolve("sprint4-test.db");

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

        pizza = new MenuItem(
                "Margherita",
                25.0,
                true
        );

        burger = new MenuItem(
                "Classic Burger",
                30.0,
                true
        );

        restaurant.addMenuItem(pizza);
        restaurant.addMenuItem(burger);

        seedDatabase();
    }

    // =====================================================
    // DATABASE SEEDING
    // =====================================================

    private void seedDatabase() throws SQLException {

        String insertUserSql = """
                INSERT INTO users (
                    username,
                    password_hash,
                    role
                )
                VALUES (?, ?, ?)
                """;

        String insertRestaurantSql = """
                INSERT INTO restaurants (
                    id,
                    name,
                    is_open,
                    delivery_zone,
                    minimum_order
                )
                VALUES (1, ?, 1, ?, ?)
                """;

        String insertMenuItemSql = """
                INSERT INTO menu_items (
                    restaurant_id,
                    name,
                    price,
                    available
                )
                VALUES (1, ?, ?, ?)
                """;

        try (Connection connection = databaseManager.getConnection()) {

            // Save customer
            try (PreparedStatement statement =
                         connection.prepareStatement(insertUserSql)) {

                statement.setString(1, customer.getUsername());
                statement.setString(2, customer.getPasswordHash());
                statement.setString(3, customer.getRole().name());

                statement.executeUpdate();
            }

            // Save restaurant
            try (PreparedStatement statement =
                         connection.prepareStatement(insertRestaurantSql)) {

                statement.setString(1, restaurant.getName());
                statement.setString(2, restaurant.getDeliveryZone());
                statement.setDouble(3, restaurant.getMinimumOrder());

                statement.executeUpdate();
            }

            // Save menu items
            try (PreparedStatement statement =
                         connection.prepareStatement(insertMenuItemSql)) {

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
    // HELPER: CREATE ORDER
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
    // TEST 1: DELIVERY FEE PERSISTENCE
    // =====================================================

    @Test
    void deliveryFeeIsPersisted() {

        Order order = createOrder("ORD-S4-001");

        repository.save(order);

        // Delivery fee = 5 + (2 * 4) = 13 NIS
        order.calculateDeliveryFee(4.0);

        repository.save(order);

        Order retrieved =
                repository.findById("ORD-S4-001");

        assertNotNull(retrieved);

        assertEquals(
                4.0,
                retrieved.getDeliveryDistanceKm(),
                0.001
        );

        assertEquals(
                13.0,
                retrieved.getDeliveryFee(),
                0.001
        );

        assertEquals(
                80.0,
                retrieved.getTotal(),
                0.001
        );

        assertEquals(
                93.0,
                retrieved.getFinalTotal(),
                0.001
        );
    }

    // =====================================================
    // TEST 2: NORMAL PRICING PERSISTENCE
    // =====================================================

    @Test
    void normalPricingIsPersisted() {

        Order order = createOrder("ORD-S4-002");

        order.calculateDeliveryFee(4.0);

        order.setPricingStrategy(
                new NormalPricingStrategy()
        );

        repository.save(order);

        Order retrieved =
                repository.findById("ORD-S4-002");

        assertNotNull(retrieved);

        assertInstanceOf(
                NormalPricingStrategy.class,
                retrieved.getPricingStrategy()
        );

        assertEquals(
                93.0,
                retrieved.getDynamicFinalTotal(),
                0.001
        );
    }

    // =====================================================
    // TEST 3: EXPRESS PRICING PERSISTENCE
    // =====================================================

    @Test
    void expressPricingIsPersisted() {

        Order order = createOrder("ORD-S4-003");

        order.setPricingStrategy(
                new ExpressPricingStrategy()
        );

        repository.save(order);

        Order retrieved =
                repository.findById("ORD-S4-003");

        assertNotNull(retrieved);

        assertInstanceOf(
                ExpressPricingStrategy.class,
                retrieved.getPricingStrategy()
        );

        double expected =
                new ExpressPricingStrategy()
                        .calculatePrice(
                                retrieved.getFinalTotal()
                        );

        assertEquals(
                expected,
                retrieved.getDynamicFinalTotal(),
                0.001
        );
    }

    // =====================================================
    // TEST 4: PEAK HOUR PRICING PERSISTENCE
    // =====================================================

    @Test
    void peakHourPricingIsPersisted() {

        Order order = createOrder("ORD-S4-004");

        order.setPricingStrategy(
                new PeakHourPricingStrategy()
        );

        repository.save(order);

        Order retrieved =
                repository.findById("ORD-S4-004");

        assertNotNull(retrieved);

        assertInstanceOf(
                PeakHourPricingStrategy.class,
                retrieved.getPricingStrategy()
        );

        double expected =
                new PeakHourPricingStrategy()
                        .calculatePrice(
                                retrieved.getFinalTotal()
                        );

        assertEquals(
                expected,
                retrieved.getDynamicFinalTotal(),
                0.001
        );
    }

    // =====================================================
    // TEST 5: PROMOTIONAL PRICING PERSISTENCE
    // =====================================================

    @Test
    void promotionalPricingIsPersisted() {

        Order order = createOrder("ORD-S4-005");

        order.setPricingStrategy(
                new PromotionalPricingStrategy()
        );

        repository.save(order);

        Order retrieved =
                repository.findById("ORD-S4-005");

        assertNotNull(retrieved);

        assertInstanceOf(
                PromotionalPricingStrategy.class,
                retrieved.getPricingStrategy()
        );

        double expected =
                new PromotionalPricingStrategy()
                        .calculatePrice(
                                retrieved.getFinalTotal()
                        );

        assertEquals(
                expected,
                retrieved.getDynamicFinalTotal(),
                0.001
        );
    }

    // =====================================================
    // TEST 6: PAID STATUS PERSISTENCE
    // =====================================================

    @Test
    void paidStatusIsPersisted() {

        Order order = createOrder("ORD-S4-006");

        repository.save(order);

        order.setPaymentStatus(PaymentStatus.PAID);

        repository.save(order);

        Order retrieved =
                repository.findById("ORD-S4-006");

        assertNotNull(retrieved);

        assertEquals(
                PaymentStatus.PAID,
                retrieved.getPaymentStatus()
        );
    }

    // =====================================================
    // TEST 7: FAILED PAYMENT PERSISTENCE
    // =====================================================

    @Test
    void failedPaymentIsPersisted() {

        Order order = createOrder("ORD-S4-007");

        repository.save(order);

        order.setPaymentStatus(PaymentStatus.FAILED);

        repository.save(order);

        Order retrieved =
                repository.findById("ORD-S4-007");

        assertNotNull(retrieved);

        assertEquals(
                PaymentStatus.FAILED,
                retrieved.getPaymentStatus()
        );
    }

    // =====================================================
    // TEST 8: ALL SPRINT 4 FIELDS SURVIVE RESTART
    // =====================================================

    @Test
    void allSprint4FieldsSurviveRestart() {

        Order order = createOrder("ORD-S4-008");

        // Calculate delivery fee
        order.calculateDeliveryFee(4.0);

        // Apply peak-hour pricing
        order.setPricingStrategy(
                new PeakHourPricingStrategy()
        );

        // Mark payment as completed
        order.setPaymentStatus(PaymentStatus.PAID);

        repository.save(order);

        // Simulate application restart by creating
        // a new database manager and repository.
        DatabaseManager secondDatabaseManager =
                new DatabaseManager(
                        "jdbc:sqlite:" + dbPath.toAbsolutePath()
                );

        SqliteOrderRepository secondRepository =
                new SqliteOrderRepository(secondDatabaseManager);

        Order retrieved =
                secondRepository.findById("ORD-S4-008");

        assertNotNull(retrieved);

        // Food total
        assertEquals(
                80.0,
                retrieved.getTotal(),
                0.001
        );

        // Delivery distance
        assertEquals(
                4.0,
                retrieved.getDeliveryDistanceKm(),
                0.001
        );

        // Delivery fee
        assertEquals(
                13.0,
                retrieved.getDeliveryFee(),
                0.001
        );

        // Pricing strategy
        assertInstanceOf(
                PeakHourPricingStrategy.class,
                retrieved.getPricingStrategy()
        );

        // Payment status
        assertEquals(
                PaymentStatus.PAID,
                retrieved.getPaymentStatus()
        );

        // Order items should remain intact
        assertEquals(
                2,
                retrieved.getItems().size()
        );

        // Dynamic total should use the saved strategy
        double expected =
                new PeakHourPricingStrategy()
                        .calculatePrice(
                                retrieved.getFinalTotal()
                        );

        assertEquals(
                expected,
                retrieved.getDynamicFinalTotal(),
                0.001
        );
    }

    // =====================================================
    // TEST 9: PAYMENT SERVICE + SQLITE INTEGRATION
    // =====================================================

    @Test
    void successfulCashPaymentIsPersisted() {

        Order order = createOrder("ORD-S4-009");

        order.calculateDeliveryFee(4.0);

        order.setPricingStrategy(
                new NormalPricingStrategy()
        );

        repository.save(order);

        PaymentService paymentService =
                new PaymentService();

        // Pay the exact amount due
        PaymentStatus result =
                paymentService.processCashPayment(
                        order,
                        order.getDynamicFinalTotal()
                );

        assertEquals(
                PaymentStatus.PAID,
                result
        );

        repository.save(order);

        Order retrieved =
                repository.findById("ORD-S4-009");

        assertNotNull(retrieved);

        assertEquals(
                PaymentStatus.PAID,
                retrieved.getPaymentStatus()
        );
    }

    // =====================================================
    // TEST 10: FAILED CASH PAYMENT + SQLITE INTEGRATION
    // =====================================================

    @Test
    void incorrectCashPaymentIsPersisted() {

        Order order = createOrder("ORD-S4-010");

        order.calculateDeliveryFee(4.0);

        repository.save(order);

        PaymentService paymentService =
                new PaymentService();

        // Required amount = 93 NIS
        // Customer pays only 50 NIS
        PaymentStatus result =
                paymentService.processCashPayment(
                        order,
                        50.0
                );

        assertEquals(
                PaymentStatus.FAILED,
                result
        );

        repository.save(order);

        Order retrieved =
                repository.findById("ORD-S4-010");

        assertNotNull(retrieved);

        assertEquals(
                PaymentStatus.FAILED,
                retrieved.getPaymentStatus()
        );
    }
}