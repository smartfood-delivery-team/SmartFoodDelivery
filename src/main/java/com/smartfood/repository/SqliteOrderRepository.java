package com.smartfood.repository;

import com.smartfood.database.DatabaseManager;

import com.smartfood.domain.Driver;
import com.smartfood.domain.MenuItem;
import com.smartfood.domain.Order;
import com.smartfood.domain.OrderItem;
import com.smartfood.domain.OrderStatus;
import com.smartfood.domain.PaymentStatus;
import com.smartfood.domain.Restaurant;
import com.smartfood.domain.Role;
import com.smartfood.domain.User;

import com.smartfood.pricing.ExpressPricingStrategy;
import com.smartfood.pricing.NormalPricingStrategy;
import com.smartfood.pricing.PeakHourPricingStrategy;
import com.smartfood.pricing.PricingStrategy;
import com.smartfood.pricing.PromotionalPricingStrategy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite implementation of OrderRepository.
 *
 * Sprint 2:
 * - Save orders and order items.
 * - Retrieve orders.
 * - Update order statuses.
 *
 * Sprint 3:
 * - Persist assigned drivers.
 *
 * Sprint 4:
 * - Persist delivery distance and fee.
 * - Persist pricing strategies.
 * - Persist payment status.
 */
public class SqliteOrderRepository implements OrderRepository {

    private final DatabaseManager databaseManager;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public SqliteOrderRepository(
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
    // SAVE ORDER
    // =====================================================

    @Override
    public void save(Order order) {

        if (order == null) {
            throw new IllegalArgumentException(
                    "Order cannot be null"
            );
        }

        String checkOrderSql = """
                SELECT 1
                FROM orders
                WHERE id = ?
                """;

        String updateOrderSql = """
                UPDATE orders
                SET
                    status = ?,
                    assigned_driver_id = ?,
                    delivery_distance_km = ?,
                    delivery_fee = ?,
                    pricing_strategy = ?,
                    payment_status = ?
                WHERE id = ?
                """;

        String findRestaurantSql = """
                SELECT id
                FROM restaurants
                WHERE name = ?
                """;

        String insertOrderSql = """
                INSERT INTO orders (
                    id,
                    customer_username,
                    restaurant_id,
                    delivery_address,
                    status,
                    total,
                    assigned_driver_id,
                    delivery_distance_km,
                    delivery_fee,
                    pricing_strategy,
                    payment_status
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        String findMenuSql = """
                SELECT
                    id,
                    price,
                    available
                FROM menu_items
                WHERE restaurant_id = ?
                  AND name = ?
                """;

        String insertOrderItemSql = """
                INSERT INTO order_items (
                    order_id,
                    menu_item_id,
                    quantity,
                    unit_price
                )
                VALUES (?, ?, ?, ?)
                """;

        try (
                Connection connection =
                        databaseManager.getConnection()
        ) {

            connection.setAutoCommit(false);

            try {

                // ==========================================
                // STEP 1: CHECK IF ORDER EXISTS
                // ==========================================

                boolean orderExists;

                try (
                        PreparedStatement statement =
                                connection.prepareStatement(
                                        checkOrderSql
                                )
                ) {

                    statement.setString(
                            1,
                            order.getId()
                    );

                    try (
                            ResultSet result =
                                    statement.executeQuery()
                    ) {

                        orderExists = result.next();
                    }
                }

                // ==========================================
                // STEP 2: UPDATE EXISTING ORDER
                // ==========================================

                if (orderExists) {

                    try (
                            PreparedStatement statement =
                                    connection.prepareStatement(
                                            updateOrderSql
                                    )
                    ) {

                        statement.setString(
                                1,
                                order.getStatus().name()
                        );

                        setAssignedDriverParameter(
                                statement,
                                2,
                                order
                        );

                        statement.setDouble(
                                3,
                                order.getDeliveryDistanceKm()
                        );

                        statement.setDouble(
                                4,
                                order.getDeliveryFee()
                        );

                        statement.setString(
                                5,
                                getPricingStrategyName(
                                        order.getPricingStrategy()
                                )
                        );

                        statement.setString(
                                6,
                                order.getPaymentStatus().name()
                        );

                        statement.setString(
                                7,
                                order.getId()
                        );

                        statement.executeUpdate();
                    }

                } else {

                    // ==========================================
                    // STEP 3: FIND RESTAURANT ID
                    // ==========================================

                    int restaurantId;

                    try (
                            PreparedStatement statement =
                                    connection.prepareStatement(
                                            findRestaurantSql
                                    )
                    ) {

                        statement.setString(
                                1,
                                order.getRestaurant().getName()
                        );

                        try (
                                ResultSet result =
                                        statement.executeQuery()
                        ) {

                            if (!result.next()) {

                                throw new IllegalArgumentException(
                                        "Restaurant not found: "
                                                + order.getRestaurant()
                                                .getName()
                                );
                            }

                            restaurantId = result.getInt("id");
                        }
                    }

                    // ==========================================
                    // STEP 4: INSERT ORDER
                    // ==========================================

                    try (
                            PreparedStatement statement =
                                    connection.prepareStatement(
                                            insertOrderSql
                                    )
                    ) {

                        statement.setString(
                                1,
                                order.getId()
                        );

                        statement.setString(
                                2,
                                order.getCustomer().getUsername()
                        );

                        statement.setInt(
                                3,
                                restaurantId
                        );

                        statement.setString(
                                4,
                                order.getDeliveryAddress()
                        );

                        statement.setString(
                                5,
                                order.getStatus().name()
                        );

                        statement.setDouble(
                                6,
                                order.getTotal()
                        );

                        setAssignedDriverParameter(
                                statement,
                                7,
                                order
                        );

                        statement.setDouble(
                                8,
                                order.getDeliveryDistanceKm()
                        );

                        statement.setDouble(
                                9,
                                order.getDeliveryFee()
                        );

                        statement.setString(
                                10,
                                getPricingStrategyName(
                                        order.getPricingStrategy()
                                )
                        );

                        statement.setString(
                                11,
                                order.getPaymentStatus().name()
                        );

                        statement.executeUpdate();
                    }

                    // ==========================================
                    // STEP 5: INSERT ORDER ITEMS
                    // ==========================================

                    for (OrderItem item : order.getItems()) {

                        if (item == null
                                || item.getMenuItem() == null
                                || item.getQuantity() <= 0) {

                            throw new IllegalArgumentException(
                                    "Invalid order item"
                            );
                        }

                        int menuItemId;
                        double storedPrice;

                        // Find menu item in SQLite
                        try (
                                PreparedStatement statement =
                                        connection.prepareStatement(
                                                findMenuSql
                                        )
                        ) {

                            statement.setInt(
                                    1,
                                    restaurantId
                            );

                            statement.setString(
                                    2,
                                    item.getMenuItem().getName()
                            );

                            try (
                                    ResultSet result =
                                            statement.executeQuery()
                            ) {

                                if (!result.next()) {

                                    throw new IllegalArgumentException(
                                            "Menu item not found: "
                                                    + item.getMenuItem()
                                                    .getName()
                                    );
                                }

                                menuItemId =
                                        result.getInt("id");

                                storedPrice =
                                        result.getDouble("price");

                                boolean available =
                                        result.getInt("available") == 1;

                                // Reject duplicate item names
                                if (result.next()) {

                                    throw new IllegalArgumentException(
                                            "Ambiguous menu item name: "
                                                    + item.getMenuItem()
                                                    .getName()
                                    );
                                }

                                if (!available) {

                                    throw new IllegalArgumentException(
                                            "Menu item is unavailable: "
                                                    + item.getMenuItem()
                                                    .getName()
                                    );
                                }
                            }
                        }

                        // Verify the submitted item price
                        if (Double.compare(
                                item.getMenuItem().getPrice(),
                                storedPrice
                        ) != 0) {

                            throw new IllegalArgumentException(
                                    "Menu item price mismatch: "
                                            + item.getMenuItem()
                                            .getName()
                            );
                        }

                        // Insert item and historical price
                        try (
                                PreparedStatement statement =
                                        connection.prepareStatement(
                                                insertOrderItemSql
                                        )
                        ) {

                            statement.setString(
                                    1,
                                    order.getId()
                            );

                            statement.setInt(
                                    2,
                                    menuItemId
                            );

                            statement.setInt(
                                    3,
                                    item.getQuantity()
                            );

                            statement.setDouble(
                                    4,
                                    storedPrice
                            );

                            statement.executeUpdate();
                        }
                    }
                }

                // ==========================================
                // STEP 6: COMMIT TRANSACTION
                // ==========================================

                connection.commit();

            } catch (SQLException | RuntimeException e) {

                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw e;
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Failed to save order: "
                            + order.getId(),
                    e
            );
        }
    }

    // =====================================================
    // FIND ORDER BY ID
    // =====================================================

    @Override
    public Order findById(String id) {

        if (id == null || id.isBlank()) {

            throw new IllegalArgumentException(
                    "Order ID cannot be empty"
            );
        }

        String findOrderSql = """
                SELECT
                    o.id,
                    o.delivery_address,
                    o.status,
                    o.assigned_driver_id,
                    o.delivery_distance_km,
                    o.delivery_fee,
                    o.pricing_strategy,
                    o.payment_status,

                    u.username,
                    u.password_hash,
                    u.role,

                    r.name AS restaurant_name,
                    r.is_open,
                    r.delivery_zone,
                    r.minimum_order

                FROM orders o

                JOIN users u
                    ON o.customer_username = u.username

                JOIN restaurants r
                    ON o.restaurant_id = r.id

                WHERE o.id = ?
                """;

        String findOrderItemsSql = """
                SELECT
                    m.name,
                    m.available,
                    oi.quantity,
                    oi.unit_price

                FROM order_items oi

                JOIN menu_items m
                    ON oi.menu_item_id = m.id

                WHERE oi.order_id = ?

                ORDER BY oi.id
                """;

        String findDriverSql = """
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
                Connection connection =
                        databaseManager.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                findOrderSql
                        )
        ) {

            statement.setString(1, id);

            try (
                    ResultSet result =
                            statement.executeQuery()
            ) {

                if (!result.next()) {
                    return null;
                }

                // ==========================================
                // STEP 1: RECONSTRUCT CUSTOMER
                // ==========================================

                User customer = new User(
                        result.getString("username"),
                        result.getString("password_hash"),
                        Role.valueOf(
                                result.getString("role")
                        )
                );

                // ==========================================
                // STEP 2: RECONSTRUCT RESTAURANT
                // ==========================================

                Restaurant restaurant = new Restaurant(
                        result.getString("restaurant_name"),
                        result.getInt("is_open") == 1,
                        result.getString("delivery_zone"),
                        result.getDouble("minimum_order")
                );

                String deliveryAddress =
                        result.getString("delivery_address");

                OrderStatus status =
                        OrderStatus.valueOf(
                                result.getString("status")
                        );

                // ==========================================
                // STEP 3: LOAD ORDER ITEMS
                // ==========================================

                List<OrderItem> items =
                        new ArrayList<>();

                try (
                        PreparedStatement itemStatement =
                                connection.prepareStatement(
                                        findOrderItemsSql
                                )
                ) {

                    itemStatement.setString(1, id);

                    try (
                            ResultSet itemResult =
                                    itemStatement.executeQuery()
                    ) {

                        while (itemResult.next()) {

                            // Use historical saved price
                            MenuItem menuItem = new MenuItem(
                                    itemResult.getString("name"),
                                    itemResult.getDouble("unit_price"),
                                    itemResult.getInt("available") == 1
                            );

                            OrderItem orderItem = new OrderItem(
                                    menuItem,
                                    itemResult.getInt("quantity")
                            );

                            items.add(orderItem);
                        }
                    }
                }

                // ==========================================
                // STEP 4: RECONSTRUCT ORDER
                // ==========================================

                Order order = new Order(
                        id,
                        customer,
                        restaurant,
                        items,
                        deliveryAddress
                );

                // Restore status
                order.setStatus(status);

                // ==========================================
                // STEP 5: RESTORE ASSIGNED DRIVER
                // ==========================================

                String assignedDriverId =
                        result.getString("assigned_driver_id");

                if (assignedDriverId != null) {

                    try (
                            PreparedStatement driverStatement =
                                    connection.prepareStatement(
                                            findDriverSql
                                    )
                    ) {

                        driverStatement.setString(
                                1,
                                assignedDriverId
                        );

                        try (
                                ResultSet driverResult =
                                        driverStatement.executeQuery()
                        ) {

                            if (!driverResult.next()) {

                                throw new IllegalStateException(
                                        "Assigned driver not found: "
                                                + assignedDriverId
                                );
                            }

                            Driver driver = new Driver(
                                    driverResult.getString("id"),
                                    driverResult.getString("name"),
                                    driverResult.getInt("available") == 1,
                                    driverResult.getDouble("distance"),
                                    driverResult.getInt("estimated_time")
                            );

                            // Restore without assigning again
                            order.restoreAssignedDriver(driver);
                        }
                    }
                }

                // ==========================================
                // STEP 6: RESTORE DELIVERY FEE
                // ==========================================

                double distanceKm =
                        result.getDouble("delivery_distance_km");

                double savedDeliveryFee =
                        result.getDouble("delivery_fee");

                if (distanceKm > 0 || savedDeliveryFee > 0) {

                    order.calculateDeliveryFee(distanceKm);
                }

                // ==========================================
                // STEP 7: RESTORE PRICING STRATEGY
                // ==========================================

                String strategyName =
                        result.getString("pricing_strategy");

                order.setPricingStrategy(
                        createPricingStrategy(strategyName)
                );

                // ==========================================
                // STEP 8: RESTORE PAYMENT STATUS
                // ==========================================

                PaymentStatus paymentStatus =
                        PaymentStatus.valueOf(
                                result.getString("payment_status")
                        );

                order.setPaymentStatus(paymentStatus);

                return order;
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Failed to retrieve order: " + id,
                    e
            );
        }
    }

    // =====================================================
    // FIND ALL ORDERS
    // =====================================================

    @Override
    public List<Order> findAll() {

        String sql = """
                SELECT id
                FROM orders
                ORDER BY rowid
                """;

        List<String> orderIds =
                new ArrayList<>();

        try (
                Connection connection =
                        databaseManager.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet result =
                        statement.executeQuery()
        ) {

            while (result.next()) {

                orderIds.add(
                        result.getString("id")
                );
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Failed to retrieve orders",
                    e
            );
        }

        List<Order> orders =
                new ArrayList<>();

        for (String orderId : orderIds) {

            Order order = findById(orderId);

            if (order != null) {
                orders.add(order);
            }
        }

        return orders;
    }

    // =====================================================
    // HELPER: SET ASSIGNED DRIVER PARAMETER
    // =====================================================

    private void setAssignedDriverParameter(
            PreparedStatement statement,
            int parameterIndex,
            Order order
    ) throws SQLException {

        if (order.getAssignedDriver() != null) {

            statement.setString(
                    parameterIndex,
                    order.getAssignedDriver().getId()
            );

        } else {

            statement.setNull(
                    parameterIndex,
                    Types.VARCHAR
            );
        }
    }

    // =====================================================
    // HELPER: CONVERT PRICING STRATEGY TO TEXT
    // =====================================================

    private String getPricingStrategyName(
            PricingStrategy strategy
    ) {

        if (strategy == null
                || strategy instanceof NormalPricingStrategy) {

            return "NORMAL";
        }

        if (strategy instanceof ExpressPricingStrategy) {

            return "EXPRESS";
        }

        if (strategy instanceof PeakHourPricingStrategy) {

            return "PEAK_HOUR";
        }

        if (strategy instanceof PromotionalPricingStrategy) {

            return "PROMOTIONAL";
        }

        throw new IllegalArgumentException(
                "Unsupported pricing strategy: "
                        + strategy.getClass().getSimpleName()
        );
    }

    // =====================================================
    // HELPER: CONVERT TEXT TO PRICING STRATEGY
    // =====================================================

    private PricingStrategy createPricingStrategy(
            String name
    ) {

        if (name == null) {

            return new NormalPricingStrategy();
        }

        return switch (name) {

            case "NORMAL" ->
                    new NormalPricingStrategy();

            case "EXPRESS" ->
                    new ExpressPricingStrategy();

            case "PEAK_HOUR" ->
                    new PeakHourPricingStrategy();

            case "PROMOTIONAL" ->
                    new PromotionalPricingStrategy();

            default ->
                    throw new IllegalArgumentException(
                            "Unknown pricing strategy: "
                                    + name
                    );
        };
    }
}