package com.smartfood.service;

import com.smartfood.domain.*;
import com.smartfood.repository.InMemoryRestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RestaurantServiceTest {
    private RestaurantService service;

    @BeforeEach
    void setUp() {
        InMemoryRestaurantRepository repo = new InMemoryRestaurantRepository();
        Restaurant open = new Restaurant("Pizza House", true);
        open.addMenuItem(new MenuItem("Margherita", 25.0, true));
        open.addMenuItem(new MenuItem("Pepperoni", 30.0, false));
        repo.save(open);
        repo.save(new Restaurant("Closed Cafe", false));
        service = new RestaurantService(repo);
    }

    @Test
    void onlyOpenRestaurantsDisplayed() {
        List<Restaurant> result = service.getOpenRestaurants();
        assertEquals(1, result.size());
        assertEquals("Pizza House", result.get(0).getName());
    }

    @Test
    void onlyAvailableMenuItemsDisplayed() {
        List<MenuItem> menu = service.getAvailableMenu("Pizza House");
        assertEquals(1, menu.size());
        assertEquals("Margherita", menu.get(0).getName());
    }

    @Test
    void itemPriceCorrect() {
        MenuItem item = service.getAvailableMenu("Pizza House").get(0);
        assertEquals(25.0, item.getPrice(), 0.001);
    }

    @Test
    void unknownRestaurant_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.getAvailableMenu("Nope"));
    }
}