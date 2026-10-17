package com.smartfood.service;

import com.smartfood.domain.Driver;
import com.smartfood.domain.Order;
import com.smartfood.domain.OrderStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages observers and sends notifications
 * when an order status changes or a driver is assigned.
 */
public class OrderNotificationService {

    private final List<OrderObserver> observers = new ArrayList<>();

    /**
     * Registers an observer.
     *
     * @param observer the observer to register
     */
    public void addObserver(OrderObserver observer) {

        if (observer == null) {
            throw new IllegalArgumentException(
                "Observer cannot be null"
            );
        }

        observers.add(observer);
    }

    /**
     * Removes a registered observer.
     *
     * @param observer the observer to remove
     */
    public void removeObserver(OrderObserver observer) {
        observers.remove(observer);
    }

    /**
     * Updates the order status and notifies observers.
     *
     * @param order the order to update
     * @param newStatus the new status
     */
    public void updateOrderStatus(Order order, OrderStatus newStatus) {

        if (order == null || newStatus == null) {
            throw new IllegalArgumentException(
                "Order and status cannot be null"
            );
        }

        order.setStatus(newStatus);

        for (OrderObserver observer : new ArrayList<>(observers)) {
            observer.onOrderStatusChanged(order, newStatus);
        }
    }

    /**
     * Notifies observers when a driver is assigned.
     * The order status remains unchanged.
     *
     * @param order the order
     * @param driver the assigned driver
     */
    public void notifyDriverAssigned(Order order, Driver driver) {

        if (order == null || driver == null) {
            throw new IllegalArgumentException(
                "Order and driver cannot be null"
            );
        }

        for (OrderObserver observer : new ArrayList<>(observers)) {
            observer.onDriverAssigned(order, driver);
        }
    }
}