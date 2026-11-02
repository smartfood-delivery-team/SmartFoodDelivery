package com.smartfood.repository;

import com.smartfood.domain.Order;
import java.util.List;

/**
 * Defines persistence operations for customer orders.
 *
 * Implementations may store orders in memory or SQLite.
 */
public interface OrderRepository {

    /**
     * Saves a new order or persists changes to an existing order.
     */
    void save(Order order);

    /**
     * Finds an order using its unique identifier.
     *
     * @return the order, or null if it does not exist
     */
    Order findById(String id);

    /**
     * Returns all stored orders.
     */
    List<Order> findAll();
}