package com.smartfood.presentation;

import com.smartfood.database.DatabaseInitializer;
import com.smartfood.database.DatabaseManager;
import com.smartfood.database.DataSeeder;

import com.smartfood.repository.RestaurantRepository;
import com.smartfood.repository.SqliteRestaurantRepository;
import com.smartfood.repository.SqliteUserRepository;
import com.smartfood.repository.UserRepository;

import com.smartfood.security.PasswordHasher;

import com.smartfood.service.AuthService;
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
            DatabaseManager databaseManager = new DatabaseManager();

            // Step 2: Create database tables if they do not exist
            DatabaseInitializer initializer =
                    new DatabaseInitializer(databaseManager);

            initializer.initialize();

            // Step 3: Create SQLite repositories
            UserRepository userRepository =
                    new SqliteUserRepository(databaseManager);

            RestaurantRepository restaurantRepository =
                    new SqliteRestaurantRepository(databaseManager);

            // Step 4: Create password hasher
            PasswordHasher passwordHasher = new PasswordHasher();

            // Step 5: Insert demo data only when missing
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

            // Step 7: Start the interactive console
            Scanner scanner = new Scanner(System.in);

            ConsoleMenu console = new ConsoleMenu(
                    authService,
                    restaurantService,
                    scanner
            );

            console.start();

        } catch (Exception e) {

            System.err.println(
                    "Application failed to start: " + e.getMessage()
            );

            // Keep the failure visible to callers and tests.
            throw new IllegalStateException(
                    "Smart Food Delivery startup failed", e
            );
        }
    }
}