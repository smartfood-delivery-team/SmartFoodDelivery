package com.smartfood.database;

import com.smartfood.domain.MenuItem;
import com.smartfood.domain.Restaurant;
import com.smartfood.domain.Role;
import com.smartfood.domain.User;

import com.smartfood.repository.RestaurantRepository;
import com.smartfood.repository.UserRepository;

import com.smartfood.security.PasswordHasher;

public class DataSeeder {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final PasswordHasher passwordHasher;

    public DataSeeder(
            UserRepository userRepository,
            RestaurantRepository restaurantRepository,
            PasswordHasher passwordHasher
    ) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.passwordHasher = passwordHasher;
    }

    public void seed() {
        seedUsers();
        seedRestaurants();
    }

    private void seedUsers() {

        // Create sample customer if missing
        if (userRepository.findByUsername("lana") == null) {

            User customer = new User(
                    "lana",
                    passwordHasher.hash("1234"),
                    Role.CUSTOMER
            );

            userRepository.save(customer);
        }

        // Create sample manager if missing
        if (userRepository.findByUsername("admin") == null) {

            User manager = new User(
                    "admin",
                    passwordHasher.hash("admin123"),
                    Role.MANAGER
            );

            userRepository.save(manager);
        }
    }

    private void seedRestaurants() {

        // Restaurant 1: Pizza House
        if (restaurantRepository.findByName("Pizza House") == null) {

            Restaurant pizzaHouse = new Restaurant(
                    "Pizza House",
                    true,
                    "Nablus",
                    30.0
            );

            pizzaHouse.addMenuItem(
                    new MenuItem("Margherita", 25.0, true)
            );

            pizzaHouse.addMenuItem(
                    new MenuItem("Pepperoni", 30.0, false)
            );

            pizzaHouse.addMenuItem(
                    new MenuItem("Chicken Pizza", 35.0, true)
            );

            restaurantRepository.save(pizzaHouse);
        }

        // Restaurant 2: Burger Station
        if (restaurantRepository.findByName("Burger Station") == null) {

            Restaurant burgerStation = new Restaurant(
                    "Burger Station",
                    true,
                    "Nablus",
                    20.0
            );

            burgerStation.addMenuItem(
                    new MenuItem("Classic Burger", 30.0, true)
            );

            burgerStation.addMenuItem(
                    new MenuItem("Cheese Burger", 35.0, true)
            );

            restaurantRepository.save(burgerStation);
        }

        // Restaurant 3: Closed Cafe
        if (restaurantRepository.findByName("Closed Cafe") == null) {

            Restaurant closedCafe = new Restaurant(
                    "Closed Cafe",
                    false,
                    "Nablus",
                    10.0
            );

            closedCafe.addMenuItem(
                    new MenuItem("Coffee", 12.0, true)
            );

            restaurantRepository.save(closedCafe);
        }
    }
}