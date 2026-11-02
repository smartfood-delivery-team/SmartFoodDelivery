package com.smartfood.presentation;

import com.smartfood.database.DatabaseInitializer;
import com.smartfood.database.DatabaseManager;
import com.smartfood.database.DataSeeder;

import com.smartfood.repository.OrderRepository;
import com.smartfood.repository.RestaurantRepository;
import com.smartfood.repository.SqliteOrderRepository;
import com.smartfood.repository.SqliteRestaurantRepository;
import com.smartfood.repository.SqliteUserRepository;
import com.smartfood.repository.UserRepository;

import com.smartfood.security.PasswordHasher;

import com.smartfood.service.AuthService;
import com.smartfood.service.OrderNotificationService;
import com.smartfood.service.OrderService;
import com.smartfood.service.RestaurantService;

import java.util.Scanner;

/**
 * Main entry point for the Smart Food Delivery application.
 *
 * Initializes the SQLite database, seeds demo data,
 * creates the required services, and launches the
 * interactive console.
 */
public class Main {

    public static void main(String[] args) {

        try {
            // Step 1: Connect to SQLite
            DatabaseManager databaseManager =
                    new DatabaseManager();

            // Step 2: Initialize database tables
            DatabaseInitializer initializer =
                    new DatabaseInitializer(databaseManager);

            initializer.initialize();

            // Step 3: Create SQLite repositories
            UserRepository userRepository =
                    new SqliteUserRepository(databaseManager);

            RestaurantRepository restaurantRepository =
                    new SqliteRestaurantRepository(databaseManager);

            OrderRepository orderRepository =
                    new SqliteOrderRepository(databaseManager);

            // Step 4: Create password hasher
            PasswordHasher passwordHasher =
                    new PasswordHasher();

            // Step 5: Seed demo data
            DataSeeder seeder = new DataSeeder(
                    userRepository,
                    restaurantRepository,
                    passwordHasher
            );

            seeder.seed();

            // Step 6: Create services
            AuthService authService = new AuthService(
                    userRepository,
                    passwordHasher
            );

            RestaurantService restaurantService =
                    new RestaurantService(restaurantRepository);

            OrderNotificationService notificationService =
                    new OrderNotificationService();

            OrderService orderService = new OrderService(
                    orderRepository,
                    notificationService
            );

            // Step 7: Start interactive console
            Scanner scanner = new Scanner(System.in);

            ConsoleMenu console = new ConsoleMenu(
                    authService,
                    restaurantService,
                    orderService,
                    scanner
            );

            console.start();

        } catch (Exception e) {

            System.err.println(
                    "Application failed to start: "
                            + e.getMessage()
            );

            throw new IllegalStateException(
                    "Smart Food Delivery startup failed",
                    e
            );
        }
    }
}