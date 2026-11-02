package com.smartfood.domain;

import java.util.ArrayList;
import java.util.List;

import com.smartfood.pricing.PricingStrategy;

/**
 * Represents a customer order placed at a restaurant.
 *
 * Supports order lifecycle, driver assignment,
 * delivery fees, dynamic pricing, and payment status.
 */
public class Order {

    private final String id;
    private final User customer;
    private final Restaurant restaurant;
    private final List<OrderItem> items;
    private final String deliveryAddress;

    private OrderStatus status = OrderStatus.PENDING;

    // Sprint 3 - Driver Assignment
    private Driver assignedDriver;

    // Sprint 4 - Delivery Cost
    private double deliveryDistanceKm;
    private double deliveryFee;

    // Sprint 4 - Dynamic Pricing
    private PricingStrategy pricingStrategy;

    // Sprint 4 - Payment
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    /**
     * Creates a new order with initial status PENDING.
     *
     * @param id unique order identifier
     * @param customer customer placing the order
     * @param restaurant restaurant receiving the order
     * @param items ordered items
     * @param deliveryAddress delivery destination
     */
    public Order(
            String id,
            User customer,
            Restaurant restaurant,
            List<OrderItem> items,
            String deliveryAddress
    ) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Order ID cannot be empty"
            );
        }

        if (customer == null) {
            throw new IllegalArgumentException(
                    "Customer cannot be null"
            );
        }

        if (restaurant == null) {
            throw new IllegalArgumentException(
                    "Restaurant cannot be null"
            );
        }

        if (items == null) {
            throw new IllegalArgumentException(
                    "Order items cannot be null"
            );
        }

        if (deliveryAddress == null || deliveryAddress.isBlank()) {
            throw new IllegalArgumentException(
                    "Delivery address cannot be empty"
            );
        }

        this.id = id;
        this.customer = customer;
        this.restaurant = restaurant;
        this.items = new ArrayList<>(items);
        this.deliveryAddress = deliveryAddress;

        this.deliveryDistanceKm = 0.0;
        this.deliveryFee = 0.0;
    }

    // ==========================================
    // Basic Order Information
    // ==========================================

    public String getId() {
        return id;
    }

    public User getCustomer() {
        return customer;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public List<OrderItem> getItems() {
        return new ArrayList<>(items);
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {

        if (status == null) {
            throw new IllegalArgumentException(
                    "Order status cannot be null"
            );
        }

        this.status = status;
    }

    /**
     * Calculates the food subtotal.
     *
     * @return total cost of food items
     */
    public double getTotal() {

        double total = 0.0;

        for (OrderItem item : items) {
            total += item.getSubtotal();
        }

        return total;
    }

    // ==========================================
    // Sprint 3 - Driver Assignment
    // ==========================================

    /**
     * Returns the assigned driver.
     *
     * @return assigned driver or null
     */
    public Driver getAssignedDriver() {
        return assignedDriver;
    }

    /**
     * Assigns an available driver to this order.
     *
     * @param driver driver to assign
     */
    public void assignDriver(Driver driver) {

        if (driver == null) {
            throw new IllegalArgumentException(
                "Driver cannot be null"
            );
        }

        if (assignedDriver != null) {
            throw new IllegalStateException(
                "Order already has an assigned driver"
            );
        }

        driver.assign();
        this.assignedDriver = driver;
    }

    // ==========================================
    // Sprint 4 - Delivery Cost
    // ==========================================

    /**
     * Calculates delivery fees based on distance.
     *
     * Base fee = 5.0
     * Fee per kilometer = 2.0
     *
     * @param distanceKm delivery distance in kilometers
     */
    public void calculateDeliveryFee(double distanceKm) {

        if (!Double.isFinite(distanceKm) || distanceKm < 0) {
            throw new IllegalArgumentException(
                "Delivery distance must be a non-negative finite number"
            );
        }

        final double baseFee = 5.0;
        final double feePerKm = 2.0;

        this.deliveryDistanceKm = distanceKm;
        this.deliveryFee = baseFee + (distanceKm * feePerKm);
    }

    public double getDeliveryDistanceKm() {
        return deliveryDistanceKm;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    /**
     * Returns food subtotal plus delivery fee.
     *
     * @return total before dynamic pricing
     */
    public double getFinalTotal() {
        return getTotal() + deliveryFee;
    }

    // ==========================================
    // Sprint 4 - Dynamic Pricing
    // ==========================================

    /**
     * Sets the pricing strategy.
     *
     * @param pricingStrategy selected strategy
     */
    public void setPricingStrategy(PricingStrategy pricingStrategy) {

        if (pricingStrategy == null) {
            throw new IllegalArgumentException(
                "Pricing strategy cannot be null"
            );
        }

        this.pricingStrategy = pricingStrategy;
    }

    public PricingStrategy getPricingStrategy() {
        return pricingStrategy;
    }

    /**
     * Calculates the final amount after dynamic pricing.
     *
     * @return final bill amount
     */
    public double getDynamicFinalTotal() {

        double baseTotal = getFinalTotal();

        if (pricingStrategy == null) {
            return baseTotal;
        }

        return pricingStrategy.calculatePrice(baseTotal);
    }

    // ==========================================
    // Sprint 4 - Payment
    // ==========================================

    /**
     * Returns the current payment status.
     *
     * @return UNPAID, PAID, or FAILED
     */
    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    /**
     * Updates the payment status.
     *
     * @param paymentStatus new payment status
     */
    public void setPaymentStatus(PaymentStatus paymentStatus) {

        if (paymentStatus == null) {
            throw new IllegalArgumentException(
                "Payment status cannot be null"
            );
        }

        this.paymentStatus = paymentStatus;
    }
}