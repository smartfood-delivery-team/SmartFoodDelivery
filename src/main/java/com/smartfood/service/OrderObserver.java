package com.smartfood.service;

import com.smartfood.domain.Driver;
import com.smartfood.domain.Order;
import com.smartfood.domain.OrderStatus;

/**
 * Observer interface for order notifications.
 *
 * Supports order status changes and driver assignment events.
 */
public interface OrderObserver {

    /**
     * Called when the order status changes.
     *
     * @param order the updated order
     * @param newStatus the new order status
     */
    void onOrderStatusChanged(Order order, OrderStatus newStatus);

    /**
     * Called when a delivery driver is assigned to an order.
     *
     * @param order the order
     * @param driver the assigned driver
     */
    default void onDriverAssigned(Order order, Driver driver) {
        // Optional callback for driver assignment notifications.
    }
}