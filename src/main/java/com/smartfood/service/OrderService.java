package com.smartfood.service;

import com.smartfood.domain.Order;
import com.smartfood.domain.OrderItem;
import com.smartfood.domain.OrderStatus;
import com.smartfood.domain.Restaurant;
import com.smartfood.domain.User;
import com.smartfood.repository.OrderRepository;

import java.util.List;

/**
 * Handles placing, cancelling, and updating orders.
 */
public class OrderService {

    private final OrderRepository repository;
    private final OrderNotificationService notificationService;

    private int counter = 0;

    /**
     * Original constructor.
     * Keeps compatibility with existing Sprint 2 tests.
     *
     * @param repository the order storage
     */
    public OrderService(OrderRepository repository) {
        this(repository, new OrderNotificationService());
    }

    /**
     * Constructor with notification support.
     *
     * @param repository the order storage
     * @param notificationService the notification service
     */
    public OrderService(OrderRepository repository,
                        OrderNotificationService notificationService) {

        if (repository == null || notificationService == null) {
            throw new IllegalArgumentException(
                "Repository and notification service cannot be null"
            );
        }

        this.repository = repository;
        this.notificationService = notificationService;
    }

    /**
     * Registers an observer for order status changes.
     *
     * @param observer the observer to register
     */
    public void addObserver(OrderObserver observer) {
        notificationService.addObserver(observer);
    }

    /**
     * Removes an observer.
     *
     * @param observer the observer to remove
     */
    public void removeObserver(OrderObserver observer) {
        notificationService.removeObserver(observer);
    }

    /**
     * Places a new order after validating it.
     *
     * @param customer the customer placing the order
     * @param restaurant the restaurant
     * @param items the ordered items
     * @param deliveryAddress the delivery address
     * @param customerZone the zone of the customer
     * @return the created order with status PENDING
     * @throws IllegalArgumentException if the order is invalid
     */
    public Order placeOrder(User customer, Restaurant restaurant,
                            List<OrderItem> items,
                            String deliveryAddress, String customerZone) {

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException(
                "Order must contain at least one item"
            );
        }

        for (OrderItem item : items) {

            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException(
                    "Invalid quantity"
                );
            }

            if (!item.getMenuItem().isAvailable()) {
                throw new IllegalArgumentException(
                    "Item not available: "
                    + item.getMenuItem().getName()
                );
            }
        }

        if (deliveryAddress == null
                || deliveryAddress.trim().isEmpty()) {

            throw new IllegalArgumentException(
                "Invalid delivery address"
            );
        }

        String zone = restaurant.getDeliveryZone();

        if (!zone.isEmpty()
                && (customerZone == null
                || !zone.equalsIgnoreCase(customerZone))) {

            throw new IllegalArgumentException(
                "Address is outside the delivery zone"
            );
        }

        Order order = new Order(
            "ORD-" + (++counter),
            customer,
            restaurant,
            items,
            deliveryAddress
        );

        if (order.getTotal() < restaurant.getMinimumOrder()) {
            throw new IllegalArgumentException(
                "Order is below the minimum order value"
            );
        }

        repository.save(order);

        return order;
    }

    /**
     * Cancels an order that has not yet entered preparation.
     *
     * @param orderId the order id
     * @return the cancelled order
     * @throws IllegalArgumentException if the order does not exist
     * @throws IllegalStateException if the order can no longer be cancelled
     */
    public Order cancelOrder(String orderId) {

        Order order = repository.findById(orderId);

        if (order == null) {
            throw new IllegalArgumentException(
                "Order not found"
            );
        }

        if (order.getStatus() != OrderStatus.PENDING
                && order.getStatus() != OrderStatus.CONFIRMED) {

            throw new IllegalStateException(
                "Order can no longer be cancelled"
            );
        }

        notificationService.updateOrderStatus(
            order,
            OrderStatus.CANCELLED
        );

        return order;
    }

    /**
     * Updates the status of an existing order
     * and notifies registered observers.
     *
     * @param orderId the order ID
     * @param newStatus the new order status
     * @return the updated order
     */
    public Order updateOrderStatus(String orderId,
                                   OrderStatus newStatus) {

        Order order = repository.findById(orderId);

        if (order == null) {
            throw new IllegalArgumentException(
                "Order not found"
            );
        }

        if (newStatus == null) {
            throw new IllegalArgumentException(
                "Order status cannot be null"
            );
        }

        notificationService.updateOrderStatus(
            order,
            newStatus
        );

        return order;
    }
}