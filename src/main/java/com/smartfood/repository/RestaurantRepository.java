package com.smartfood.repository;

import com.smartfood.domain.Restaurant;
import java.util.List;

public interface RestaurantRepository {

    void save(Restaurant restaurant);

    List<Restaurant> findAll();

    Restaurant findByName(String name);
}