package com.smartfood.service;

import com.smartfood.domain.Driver;
import com.smartfood.domain.Order;
import com.smartfood.domain.OrderStatus;

/**
 * Sends notifications to customers about
 * order status changes and driver assignments.
 */
public class CustomerNotificationObserver implements OrderObserver {

    /**
     * Notifies the customer when the order status changes.
     */
    @Override
    public void onOrderStatusChanged(Order order, OrderStatus newStatus) {

        if (order == null || newStatus == null) {
            throw new IllegalArgumentException(
                "Order and status cannot be null"
            );
        }

        String customerName = getCustomerName(order);

        System.out.println(
            "Notification for customer " + customerName
            + ": Order " + order.getId()
            + " status changed to " + newStatus
        );
    }

    /**
     * Notifies the customer when a driver is assigned.
     */
    @Override
    public void onDriverAssigned(Order order, Driver driver) {

        if (order == null || driver == null) {
            throw new IllegalArgumentException(
                "Order and driver cannot be null"
            );
        }

        String customerName = getCustomerName(order);

        System.out.println(
            "Notification for customer " + customerName
            + ": Driver " + driver.getName()
            + " has been assigned to order " + order.getId()
        );
    }

    /**
     * Returns the customer name or a default value.
     */
    private String getCustomerName(Order order) {

        if (order.getCustomer() == null) {
            return "Unknown";
        }

        return order.getCustomer().getUsername();
    }
}