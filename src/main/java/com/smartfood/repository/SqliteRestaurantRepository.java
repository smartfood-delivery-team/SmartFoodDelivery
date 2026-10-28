package com.smartfood.repository;

import com.smartfood.database.DatabaseManager;
import com.smartfood.domain.Restaurant;
import com.smartfood.domain.MenuItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqliteRestaurantRepository implements RestaurantRepository {

    private final DatabaseManager databaseManager;

    public SqliteRestaurantRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public void save(Restaurant restaurant) {

        String restaurantSql = """
                INSERT INTO restaurants
                (name, is_open, delivery_zone, minimum_order)
                VALUES (?, ?, ?, ?)
                """;

        String menuSql = """
                INSERT INTO menu_items
                (restaurant_id, name, price, available)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = databaseManager.getConnection()) {

            connection.setAutoCommit(false);

            try {
                int restaurantId;

                // Step 1: Insert restaurant
                try (PreparedStatement statement =
                             connection.prepareStatement(restaurantSql)) {

                    statement.setString(1, restaurant.getName());
                    statement.setInt(2, restaurant.isOpen() ? 1 : 0);
                    statement.setString(3, restaurant.getDeliveryZone());
                    statement.setDouble(4, restaurant.getMinimumOrder());

                    statement.executeUpdate();
                }

                // Step 2: Retrieve the generated restaurant ID
                try (
                        Statement statement = connection.createStatement();
                        ResultSet result = statement.executeQuery(
                                "SELECT last_insert_rowid()"
                        )
                ) {
                    if (!result.next()) {
                        throw new SQLException("Restaurant ID not generated");
                    }

                    restaurantId = result.getInt(1);
                }

                // Step 3: Insert all menu items
                try (PreparedStatement statement =
                             connection.prepareStatement(menuSql)) {

                    for (MenuItem item : restaurant.getMenu()) {

                        statement.setInt(1, restaurantId);
                        statement.setString(2, item.getName());
                        statement.setDouble(3, item.getPrice());
                        statement.setInt(4, item.isAvailable() ? 1 : 0);

                        statement.executeUpdate();
                    }
                }

                // Step 4: Save all changes permanently
                connection.commit();

            } catch (SQLException | RuntimeException e) {

                // Undo the entire transaction if anything fails
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw e;
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save restaurant", e);
        }
    }

    @Override
    public List<Restaurant> findAll() {

        String sql = """
                SELECT id, name, is_open, delivery_zone, minimum_order
                FROM restaurants
                ORDER BY name
                """;

        List<Restaurant> restaurants = new ArrayList<>();
        List<Integer> restaurantIds = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection()) {

            // Step 1: Retrieve all restaurants
            try (
                    PreparedStatement statement =
                            connection.prepareStatement(sql);

                    ResultSet result = statement.executeQuery()
            ) {

                while (result.next()) {

                    int id = result.getInt("id");

                    String name = result.getString("name");

                    boolean open = result.getInt("is_open") == 1;

                    String zone = result.getString("delivery_zone");

                    double minimumOrder =
                            result.getDouble("minimum_order");

                    Restaurant restaurant = new Restaurant(
                            name,
                            open,
                            zone,
                            minimumOrder
                    );

                    restaurants.add(restaurant);
                    restaurantIds.add(id);
                }
            }

            // Step 2: Load each restaurant's menu
            for (int i = 0; i < restaurants.size(); i++) {

                loadMenu(
                        connection,
                        restaurantIds.get(i),
                        restaurants.get(i)
                );
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Failed to retrieve restaurants", e
            );
        }

        return restaurants;
    }

    @Override
    public Restaurant findByName(String name) {

        String sql = """
                SELECT id, name, is_open, delivery_zone, minimum_order
                FROM restaurants
                WHERE name = ? COLLATE NOCASE
                """;

        try (Connection connection = databaseManager.getConnection()) {

            Restaurant restaurant;
            int restaurantId;

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, name);

                try (ResultSet result = statement.executeQuery()) {

                    if (!result.next()) {
                        return null;
                    }

                    restaurantId = result.getInt("id");

                    String foundName = result.getString("name");

                    boolean open = result.getInt("is_open") == 1;

                    String zone = result.getString("delivery_zone");

                    double minimumOrder =
                            result.getDouble("minimum_order");

                    restaurant = new Restaurant(
                            foundName,
                            open,
                            zone,
                            minimumOrder
                    );
                }
            }

            // Load the restaurant's menu
            loadMenu(connection, restaurantId, restaurant);

            return restaurant;

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Failed to find restaurant", e
            );
        }
    }

    /**
     * Loads menu items from SQLite into a restaurant object.
     */
    private void loadMenu(
            Connection connection,
            int restaurantId,
            Restaurant restaurant
    ) throws SQLException {

        String sql = """
                SELECT name, price, available
                FROM menu_items
                WHERE restaurant_id = ?
                ORDER BY id
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, restaurantId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    String itemName = result.getString("name");

                    double price = result.getDouble("price");

                    boolean available =
                            result.getInt("available") == 1;

                    MenuItem item = new MenuItem(
                            itemName,
                            price,
                            available
                    );

                    restaurant.addMenuItem(item);
                }
            }
        }
    }
}