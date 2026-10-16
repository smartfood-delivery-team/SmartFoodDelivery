package com.smartfood.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * A restaurant with a menu, an open/closed status,
 * a delivery zone and a minimum order value.
 */
public class Restaurant {
    private final String name;
    private boolean open;
    private final String deliveryZone;
    private final double minimumOrder;
    private final List<MenuItem> menu = new ArrayList<>();

    /**
     * Creates a restaurant with no zone restriction and no minimum order.
     * @param name restaurant name
     * @param open whether it is currently open
     */
    public Restaurant(String name, boolean open) {
        this(name, open, "", 0.0);
    }

    /**
     * Creates a restaurant.
     * @param name restaurant name
     * @param open whether it is currently open
     * @param deliveryZone the zone the restaurant delivers to
     * @param minimumOrder the minimum order value
     */
    public Restaurant(String name, boolean open, String deliveryZone, double minimumOrder) {
        this.name = name;
        this.open = open;
        this.deliveryZone = deliveryZone;
        this.minimumOrder = minimumOrder;
    }

    /** @return restaurant name */
    public String getName() { return name; }

    /** @return true if open */
    public boolean isOpen() { return open; }

    /** @param open new open status */
    public void setOpen(boolean open) { this.open = open; }

    /** @return the delivery zone */
    public String getDeliveryZone() { return deliveryZone; }

    /** @return the minimum order value */
    public double getMinimumOrder() { return minimumOrder; }

    /** @return the full menu (including unavailable items) */
    public List<MenuItem> getMenu() { return menu; }

    /** @param item item to add to the menu */
    public void addMenuItem(MenuItem item) { menu.add(item); }
}