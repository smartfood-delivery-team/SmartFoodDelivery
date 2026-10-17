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
 *
 * Supports order lifecycle validation and customer
 * notifications when order status changes.
 */
public class OrderService {

    private final OrderRepository repository;

    private final OrderNotificationService notificationService;

    private int counter = 0;

    /**
     * Creates an OrderService with default notification support.
     *
     * @param repository order repository
     */
    public OrderService(OrderRepository repository) {

        this(repository, new OrderNotificationService());
    }

    /**
     * Creates an OrderService with notification support.
     *
     * @param repository order repository
     * @param notificationService notification service
     */
    public OrderService(
            OrderRepository repository,
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
     * Registers an observer for order notifications.
     *
     * @param observer observer to register
     */
    public void addObserver(OrderObserver observer) {

        notificationService.addObserver(observer);
    }

    /**
     * Removes a registered observer.
     *
     * @param observer observer to remove
     */
    public void removeObserver(OrderObserver observer) {

        notificationService.removeObserver(observer);
    }

    /**
     * Places a new order after validating its details.
     *
     * @param customer customer placing the order
     * @param restaurant selected restaurant
     * @param items ordered items
     * @param deliveryAddress delivery address
     * @param customerZone customer delivery zone
     * @return newly created order
     * @throws IllegalArgumentException if order details are invalid
     */
    public Order placeOrder(
            User customer,
            Restaurant restaurant,
            List<OrderItem> items,
            String deliveryAddress,
            String customerZone) {

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
     * Cancels an order before preparation begins.
     *
     * Only PENDING and CONFIRMED orders can be cancelled.
     *
     * @param orderId order identifier
     * @return cancelled order
     * @throws IllegalArgumentException if order is not found
     * @throws IllegalStateException if cancellation is not allowed
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
     * Updates an order status only when the transition
     * follows the permitted order lifecycle.
     *
     * Lifecycle:
     * PENDING -> CONFIRMED -> PREPARING -> READY
     * -> OUT_FOR_DELIVERY -> DELIVERED
     *
     * DELIVERED and CANCELLED are final states.
     *
     * @param orderId order identifier
     * @param newStatus requested new status
     * @return updated order
     * @throws IllegalArgumentException if the order is not found
     *         or the new status is null
     * @throws IllegalStateException if the transition is invalid
     */
    public Order updateOrderStatus(
            String orderId,
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

        OrderStatus currentStatus = order.getStatus();

        if (!isValidTransition(currentStatus, newStatus)) {

            throw new IllegalStateException(
                "Invalid order status transition: "
                + currentStatus + " -> " + newStatus
            );
        }

        notificationService.updateOrderStatus(
            order,
            newStatus
        );

        return order;
    }

    /**
     * Checks whether an order status transition is allowed.
     *
     * @param current current order status
     * @param next requested order status
     * @return true if the transition is valid; false otherwise
     */
    private boolean isValidTransition(
            OrderStatus current,
            OrderStatus next) {

        if (current == null || next == null) {
            return false;
        }

        switch (current) {

            case PENDING:
                return next == OrderStatus.CONFIRMED;

            case CONFIRMED:
                return next == OrderStatus.PREPARING;

            case PREPARING:
                return next == OrderStatus.READY;

            case READY:
                return next == OrderStatus.OUT_FOR_DELIVERY;

            case OUT_FOR_DELIVERY:
                return next == OrderStatus.DELIVERED;

            case DELIVERED:
            case CANCELLED:
                return false;

            default:
                return false;
        }
    }
}