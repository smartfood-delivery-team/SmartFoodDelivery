package com.smartfood.domain;

import java.util.ArrayList;
import java.util.List;

/** A customer order placed at a restaurant. */
public class Order {
    private final String id;
    private final User customer;
    private final Restaurant restaurant;
    private final List<OrderItem> items;
    private final String deliveryAddress;
    private OrderStatus status = OrderStatus.PENDING;

    /**
     * Creates an order. The initial status is PENDING.
     * @param id unique order id
     * @param customer the customer who placed the order
     * @param restaurant the restaurant receiving the order
     * @param items the ordered items
     * @param deliveryAddress where to deliver
     */
    public Order(String id, User customer, Restaurant restaurant,
                 List<OrderItem> items, String deliveryAddress) {
        this.id = id;
        this.customer = customer;
        this.restaurant = restaurant;
        this.items = new ArrayList<>(items);
        this.deliveryAddress = deliveryAddress;
    }

    /** @return the order id */
    public String getId() { return id; }

    /** @return the customer */
    public User getCustomer() { return customer; }

    /** @return the restaurant */
    public Restaurant getRestaurant() { return restaurant; }

    /** @return a copy of the ordered items */
    public List<OrderItem> getItems() { return new ArrayList<>(items); }

    /** @return the delivery address */
    public String getDeliveryAddress() { return deliveryAddress; }

    /** @return the current status */
    public OrderStatus getStatus() { return status; }

    /** @param status the new status */
    public void setStatus(OrderStatus status) { this.status = status; }

    /** @return the sum of all item subtotals */
    public double getTotal() {
        double total = 0;
        for (OrderItem i : items) {
            total += i.getSubtotal();
        }
        return total;
    }
}