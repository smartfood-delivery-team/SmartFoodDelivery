package com.smartfood.repository;

import com.smartfood.database.DatabaseManager;
import com.smartfood.domain.*;
import com.smartfood.database.DatabaseManager;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SqliteOrderRepository implements OrderRepository {
    private final DatabaseManager databaseManager;

    public SqliteOrderRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public void save(Order order) {

        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }

        String checkOrderSql = """
            SELECT 1
            FROM orders
            WHERE id = ?
            """;

        String updateOrderSql = """
            UPDATE orders
            SET status = ?
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
                total
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        String findMenuSql = """
            SELECT id, price, available
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

        try (Connection connection = databaseManager.getConnection()) {

            connection.setAutoCommit(false);

            try {

                // ==========================================
                // STEP 1: CHECK IF ORDER EXISTS
                // ==========================================

                boolean orderExists;

                try (PreparedStatement statement =
                             connection.prepareStatement(checkOrderSql)) {

                    statement.setString(1, order.getId());

                    try (ResultSet result = statement.executeQuery()) {
                        orderExists = result.next();
                    }
                }

                // ==========================================
                // STEP 2: UPDATE EXISTING ORDER
                // ==========================================

                if (orderExists) {

                    try (PreparedStatement statement =
                                 connection.prepareStatement(updateOrderSql)) {

                        statement.setString(1, order.getStatus().name());
                        statement.setString(2, order.getId());

                        statement.executeUpdate();
                    }

                } else {

                    // ==========================================
                    // STEP 3: FIND RESTAURANT ID
                    // ==========================================

                    int restaurantId;

                    try (PreparedStatement statement =
                                 connection.prepareStatement(findRestaurantSql)) {

                        statement.setString(
                                1,
                                order.getRestaurant().getName()
                        );

                        try (ResultSet result = statement.executeQuery()) {

                            if (!result.next()) {
                                throw new IllegalArgumentException(
                                        "Restaurant not found in database"
                                );
                            }

                            restaurantId = result.getInt("id");
                        }
                    }

                    // ==========================================
                    // STEP 4: INSERT ORDER
                    // ==========================================

                    try (PreparedStatement statement =
                                 connection.prepareStatement(insertOrderSql)) {

                        statement.setString(1, order.getId());

                        statement.setString(
                                2,
                                order.getCustomer().getUsername()
                        );

                        statement.setInt(3, restaurantId);

                        statement.setString(
                                4,
                                order.getDeliveryAddress()
                        );

                        statement.setString(
                                5,
                                order.getStatus().name()
                        );

                        statement.setDouble(6, order.getTotal());

                        statement.executeUpdate();
                    }

                    // ==========================================
                    // STEP 5: INSERT ORDER ITEMS
                    // ==========================================

                    for (OrderItem item : order.getItems()) {

                        if (item == null || item.getMenuItem() == null
                                || item.getQuantity() <= 0) {
                            throw new IllegalArgumentException(
                                    "Invalid order item"
                            );
                        }

                        int menuItemId;
                        double storedPrice;

                        // Find menu item in the database
                        try (PreparedStatement statement =
                                     connection.prepareStatement(findMenuSql)) {

                            statement.setInt(1, restaurantId);

                            statement.setString(
                                    2,
                                    item.getMenuItem().getName()
                            );

                            try (ResultSet result = statement.executeQuery()) {

                                if (!result.next()) {
                                    throw new IllegalArgumentException(
                                            "Menu item not found: "
                                                    + item.getMenuItem().getName()
                                    );
                                }

                                menuItemId = result.getInt("id");
                                storedPrice = result.getDouble("price");
                                boolean available =
                                        result.getInt("available") == 1;

                                // Reject ambiguous menu item names
                                if (result.next()) {
                                    throw new IllegalArgumentException(
                                            "Multiple menu items have the same name: "
                                                    + item.getMenuItem().getName()
                                    );
                                }

                                // Reject unavailable items
                                if (!available) {
                                    throw new IllegalArgumentException(
                                            "Menu item is unavailable: "
                                                    + item.getMenuItem().getName()
                                    );
                                }
                            }
                        }

                        // Prevent altered item prices
                        if (Double.compare(
                                item.getMenuItem().getPrice(),
                                storedPrice
                        ) != 0) {
                            throw new IllegalArgumentException(
                                    "Menu item price does not match database price: "
                                            + item.getMenuItem().getName()
                            );
                        }

                        // Insert order item
                        try (PreparedStatement statement =
                                     connection.prepareStatement(insertOrderItemSql)) {

                            statement.setString(1, order.getId());
                            statement.setInt(2, menuItemId);
                            statement.setInt(3, item.getQuantity());
                            statement.setDouble(4, storedPrice);

                            statement.executeUpdate();
                        }
                    }
                }

                // ==========================================
                // STEP 6: COMMIT TRANSACTION
                // ==========================================

                connection.commit();

            } catch (SQLException | RuntimeException e) {

                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Failed to save order: " + order.getId(),
                    e
            );
        }
    }

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

        try (Connection connection = databaseManager.getConnection()) {

            // ==========================================
            // STEP 1: FIND ORDER INFORMATION
            // ==========================================

            try (PreparedStatement statement =
                         connection.prepareStatement(findOrderSql)) {

                statement.setString(1, id);

                try (ResultSet result = statement.executeQuery()) {

                    if (!result.next()) {
                        return null;
                    }

                    // ==========================================
                    // STEP 2: RECONSTRUCT CUSTOMER
                    // ==========================================

                    User customer = new User(
                            result.getString("username"),
                            result.getString("password_hash"),
                            Role.valueOf(result.getString("role"))
                    );

                    // ==========================================
                    // STEP 3: RECONSTRUCT RESTAURANT
                    // ==========================================

                    Restaurant restaurant = new Restaurant(
                            result.getString("restaurant_name"),
                            result.getInt("is_open") == 1,
                            result.getString("delivery_zone"),
                            result.getDouble("minimum_order")
                    );

                    String deliveryAddress =
                            result.getString("delivery_address");

                    OrderStatus status = OrderStatus.valueOf(
                            result.getString("status")
                    );

                    // ==========================================
                    // STEP 4: RETRIEVE ORDER ITEMS
                    // ==========================================

                    List<OrderItem> items = new ArrayList<>();

                    try (PreparedStatement itemStatement =
                                 connection.prepareStatement(findOrderItemsSql)) {

                        itemStatement.setString(1, id);

                        try (ResultSet itemResult =
                                     itemStatement.executeQuery()) {

                            while (itemResult.next()) {

                                MenuItem menuItem = new MenuItem(
                                        itemResult.getString("name"),

                                        // Historical price at order time
                                        itemResult.getDouble("unit_price"),

                                        itemResult.getInt("available") == 1
                                );

                                int quantity =
                                        itemResult.getInt("quantity");

                                OrderItem orderItem = new OrderItem(
                                        menuItem,
                                        quantity
                                );

                                items.add(orderItem);
                            }
                        }
                    }

                    // ==========================================
                    // STEP 5: RECONSTRUCT ORDER
                    // ==========================================

                    Order order = new Order(
                            id,
                            customer,
                            restaurant,
                            items,
                            deliveryAddress
                    );

                    // Restore the saved status
                    order.setStatus(status);

                    return order;
                }
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Failed to retrieve order: " + id,
                    e
            );
        }
    }

    @Override
    public List<Order> findAll() {

        String sql = """
            SELECT id
            FROM orders
            ORDER BY rowid
            """;

        List<String> orderIds = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                orderIds.add(result.getString("id"));
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Failed to retrieve orders",
                    e
            );
        }

        List<Order> orders = new ArrayList<>();

        for (String id : orderIds) {

            Order order = findById(id);

            if (order != null) {
                orders.add(order);
            }
        }

        return orders;
    }
}