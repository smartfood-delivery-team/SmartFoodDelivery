package com.smartfood.service;

import com.smartfood.domain.Order;
import com.smartfood.domain.OrderItem;
import com.smartfood.domain.OrderStatus;
import com.smartfood.domain.Restaurant;
import com.smartfood.domain.User;
import com.smartfood.repository.OrderRepository;

import java.util.List;

/** Handles placing and cancelling orders. */
public class OrderService {
    private final OrderRepository repository;
    private int counter = 0;

    /** @param repository the order storage */
    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    /**
     * Places a new order after validating it.
     * @param customer the customer placing the order
     * @param restaurant the restaurant
     * @param items the ordered items
     * @param deliveryAddress the delivery address
     * @param customerZone the zone of the customer
     * @return the created order with status PENDING
     * @throws IllegalArgumentException if the order is invalid
     */
    public Order placeOrder(User customer, Restaurant restaurant, List<OrderItem> items,
                            String deliveryAddress, String customerZone) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        for (OrderItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Invalid quantity");
            }
            if (!item.getMenuItem().isAvailable()) {
                throw new IllegalArgumentException("Item not available: " + item.getMenuItem().getName());
            }
        }
        if (deliveryAddress == null || deliveryAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid delivery address");
        }
        String zone = restaurant.getDeliveryZone();
        if (!zone.isEmpty() && (customerZone == null || !zone.equalsIgnoreCase(customerZone))) {
            throw new IllegalArgumentException("Address is outside the delivery zone");
        }
        Order order = new Order("ORD-" + (++counter), customer, restaurant, items, deliveryAddress);
        if (order.getTotal() < restaurant.getMinimumOrder()) {
            throw new IllegalArgumentException("Order is below the minimum order value");
        }
        repository.save(order);
        return order;
    }

    /**
     * Cancels an order that has not yet entered preparation.
     * @param orderId the order id
     * @return the cancelled order
     * @throws IllegalArgumentException if the order does not exist
     * @throws IllegalStateException if the order can no longer be cancelled
     */
    public Order cancelOrder(String orderId) {
        Order order = repository.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order can no longer be cancelled");
        }
        order.setStatus(OrderStatus.CANCELLED);
        return order;
    }
}