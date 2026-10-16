package com.smartfood.domain;

/**
 * An item on a restaurant menu.
 */
public class MenuItem {
    private final String name;
    private final double price;
    private boolean available;

    /**
     * Creates a menu item.
     * @param name item name
     * @param price item price
     * @param available whether the item is currently available
     */
    public MenuItem(String name, double price, boolean available) {
        this.name = name;
        this.price = price;
        this.available = available;
    }

    /** @return item name */
    public String getName() { return name; }

    /** @return item price */
    public double getPrice() { return price; }

    /** @return true if the item is available */
    public boolean isAvailable() { return available; }

    /** @param available new availability */
    public void setAvailable(boolean available) { this.available = available; }
}