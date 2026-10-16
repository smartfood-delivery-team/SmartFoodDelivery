package com.smartfood.service;

import com.smartfood.domain.MenuItem;
import com.smartfood.domain.Restaurant;
import com.smartfood.repository.RestaurantRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Service that provides restaurant and menu browsing for customers.
 * It exposes only open restaurants and only available menu items.
 */
public class RestaurantService {

    /** Storage used to look up restaurants. */
    private final RestaurantRepository repository;

    /**
     * Creates a restaurant service.
     *
     * @param repository the restaurant storage to read from
     */
    public RestaurantService(RestaurantRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns the restaurants that are currently open.
     * Closed restaurants are hidden.
     *
     * @return a list containing only open restaurants
     */
    public List<Restaurant> getOpenRestaurants() {
        List<Restaurant> result = new ArrayList<>();
        for (Restaurant r : repository.findAll()) {
            if (r.isOpen()) {
                result.add(r);
            }
        }
        return result;
    }

    /**
     * Returns the available menu items of a restaurant.
     * Unavailable items are hidden.
     *
     * @param restaurantName the name of the restaurant
     * @return a list containing only available menu items
     * @throws IllegalArgumentException if the restaurant is not found
     */
    public List<MenuItem> getAvailableMenu(String restaurantName) {
        Restaurant r = repository.findByName(restaurantName);
        if (r == null) {
            throw new IllegalArgumentException("Restaurant not found");
        }
        List<MenuItem> result = new ArrayList<>();
        for (MenuItem item : r.getMenu()) {
            if (item.isAvailable()) {
                result.add(item);
            }
        }
        return result;
    }
}