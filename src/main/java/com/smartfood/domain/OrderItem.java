package com.smartfood.domain;

/** A menu item together with the ordered quantity. */
public class OrderItem {
    private final MenuItem menuItem;
    private final int quantity;

    /**
     * Creates an order line.
     * @param menuItem the ordered menu item
     * @param quantity the ordered quantity
     */
    public OrderItem(MenuItem menuItem, int quantity) {
        this.menuItem = menuItem;
        this.quantity = quantity;
    }

    /** @return the ordered menu item */
    public MenuItem getMenuItem() { return menuItem; }

    /** @return the ordered quantity */
    public int getQuantity() { return quantity; }

    /** @return price multiplied by quantity */
    public double getSubtotal() { return menuItem.getPrice() * quantity; }
}