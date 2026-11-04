package com.smartfood.domain;

/**
 * Represents a delivery driver in the Smart Food Delivery System.
 * A driver can be available or busy.
 */
public class Driver {

    private final String id;
    private final String name;
    private boolean available;
    private final double distance;
    private final int estimatedTime;

    /**
     * Creates a new delivery driver.
     *
     * @param id unique driver ID
     * @param name driver name
     * @param available driver availability
     * @param distance distance from the restaurant
     * @param estimatedTime estimated arrival time in minutes
     */
    public Driver(String id, String name, boolean available,
                  double distance, int estimatedTime) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Driver ID cannot be empty");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Driver name cannot be empty");
        }

        if (!Double.isFinite(distance) || distance < 0) {
            throw new IllegalArgumentException(
                    "Distance must be finite and non-negative"
            );
        }

        if (estimatedTime < 0) {
            throw new IllegalArgumentException("Estimated time cannot be negative");
        }

        this.id = id;
        this.name = name;
        this.available = available;
        this.distance = distance;
        this.estimatedTime = estimatedTime;
    }

    /**
     * Returns the driver ID.
     */
    /** Default drivers are general-purpose for backward compatibility. */
    public DriverType getDriverType() { return DriverType.GENERAL; }
    /** Return whether the driver can handle this specific order. */
    public boolean canDeliver(Order order) { return order != null; }
    /** Electric vehicle range, otherwise zero. */
    public double getRemainingRangeKm() { return 0.0; }
    public String getId() {
        return id;
    }

    /**
     * Returns the driver name.
     */
    public String getName() {
        return name;
    }

    /**
     * Checks whether the driver is available.
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Returns the distance from the restaurant.
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Returns the estimated arrival time.
     */
    public int getEstimatedTime() {
        return estimatedTime;
    }

    /**
     * Assigns the driver to an order.
     * The driver becomes busy after assignment.
     */
    public void assign() {

        if (!available) {
            throw new IllegalStateException(
                    "Driver is already busy"
            );
        }

        available = false;
    }

    /**
     * Releases the driver after completing a delivery.
     * The driver becomes available again.
     */
    public void release() {
        available = true;
    }
}