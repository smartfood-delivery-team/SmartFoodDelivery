package com.smartfood.service;

import com.smartfood.domain.MenuItem;
import com.smartfood.domain.Restaurant;
import com.smartfood.repository.RestaurantRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Service that provides restaurant and menu browsing for customers.
 * It exposes only open restaurants and available menu items.
 */
public class RestaurantService {

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
     * Returns only restaurants that are currently open.
     *
     * @return a list of open restaurants
     */
    public List<Restaurant> getOpenRestaurants() {

        List<Restaurant> result = new ArrayList<>();

        for (Restaurant restaurant : repository.findAll()) {

            if (restaurant.isOpen()) {
                result.add(restaurant);
            }
        }

        return result;
    }

    /**
     * Returns only available menu items from the specified restaurant.
     *
     * @param restaurantName the name of the restaurant
     * @return list of available menu items
     * @throws IllegalArgumentException if restaurant is not found
     */
    public List<MenuItem> getAvailableMenu(String restaurantName) {

        Restaurant restaurant = repository.findByName(restaurantName);

        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant not found");
        }

        List<MenuItem> result = new ArrayList<>();

        for (MenuItem item : restaurant.getMenu()) {

            if (item.isAvailable()) {
                result.add(item);
            }
        }

        return result;
    }
}