package com.smartfood.domain;

/**
 * Represents a menu item and its ordered quantity.
 */
public class OrderItem {

    private final MenuItem menuItem;
    private final int quantity;

    /**
     * Creates an order line.
     *
     * @param menuItem the selected menu item
     * @param quantity the requested quantity
     */
    public OrderItem(MenuItem menuItem, int quantity) {

        if (menuItem == null) {
            throw new IllegalArgumentException(
                    "Menu item cannot be null"
            );
        }

        this.menuItem = menuItem;
        this.quantity = quantity;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public int getQuantity() {
        return quantity;
    }

    /**
     * Calculates the subtotal for this order line.
     */
    public double getSubtotal() {
        return menuItem.getPrice() * quantity;
    }
}