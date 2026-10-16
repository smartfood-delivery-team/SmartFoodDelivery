package com.smartfood.repository;

import com.smartfood.domain.Order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** In-memory storage for orders. */
public class OrderRepository {
    private final Map<String, Order> orders = new HashMap<>();

    /** @param order the order to save */
    public void save(Order order) { orders.put(order.getId(), order); }

    /**
     * @param id the order id
     * @return the order or null if not found
     */
    public Order findById(String id) { return orders.get(id); }

    /** @return all orders */
    public List<Order> findAll() { return new ArrayList<>(orders.values()); }
}