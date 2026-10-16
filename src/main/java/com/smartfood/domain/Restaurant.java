package com.smartfood.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * A restaurant with a menu and an open/closed status.
 */
public class Restaurant {
    private final String name;
    private boolean open;
    private final List<MenuItem> menu = new ArrayList<>();

    /**
     * Creates a restaurant.
     * @param name restaurant name
     * @param open whether it is currently open
     */
    public Restaurant(String name, boolean open) {
        this.name = name;
        this.open = open;
    }

    /** @return restaurant name */
    public String getName() { return name; }

    /** @return true if open */
    public boolean isOpen() { return open; }

    /** @param open new open status */
    public void setOpen(boolean open) { this.open = open; }

    /** @return the full menu (including unavailable items) */
    public List<MenuItem> getMenu() { return menu; }

    /** @param item item to add to the menu */
    public void addMenuItem(MenuItem item) { menu.add(item); }
}
