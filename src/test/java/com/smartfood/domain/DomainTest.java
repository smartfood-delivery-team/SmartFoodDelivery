package com.smartfood.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DomainTest {

    @Test
    void restaurantOpenStatusCanBeChanged() {
        Restaurant r = new Restaurant("Pizza House", true);
        r.setOpen(false);
        assertFalse(r.isOpen());
    }

    @Test
    void menuItemAvailabilityCanBeChanged() {
        MenuItem item = new MenuItem("Margherita", 25.0, true);
        item.setAvailable(false);
        assertFalse(item.isAvailable());
    }
}