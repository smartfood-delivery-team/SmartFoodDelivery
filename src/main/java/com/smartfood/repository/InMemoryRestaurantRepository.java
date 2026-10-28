package com.smartfood.repository;

import com.smartfood.domain.Restaurant;
import java.util.ArrayList;
import java.util.List;

/** In-memory storage for restaurants. */
public class InMemoryRestaurantRepository implements RestaurantRepository{
    private final List<Restaurant> restaurants = new ArrayList<>();

    /** @param r restaurant to save */
    @Override
    public void save(Restaurant r) { restaurants.add(r); }

    /** @return all restaurants */
    @Override
    public List<Restaurant> findAll() { return new ArrayList<>(restaurants); }

    /**
     * @param name restaurant name
     * @return the restaurant or null
     */
    @Override
    public Restaurant findByName(String name) {
        for (Restaurant r : restaurants) {
            if (r.getName().equalsIgnoreCase(name)) return r;
        }
        return null;
    }
}