package com.smartfood.presentation;

import com.smartfood.database.DatabaseInitializer;
import com.smartfood.database.DatabaseManager;
import com.smartfood.database.DataSeeder;

import com.smartfood.domain.Driver;

import com.smartfood.repository.DriverStore;
import com.smartfood.repository.OrderRepository;
import com.smartfood.repository.RestaurantRepository;
import com.smartfood.repository.SqliteDriverRepository;
import com.smartfood.repository.SqliteOrderRepository;
import com.smartfood.repository.SqliteRestaurantRepository;
import com.smartfood.repository.SqliteUserRepository;
import com.smartfood.repository.UserRepository;

import com.smartfood.security.PasswordHasher;

import com.smartfood.service.AuthService;
import com.smartfood.service.CustomerNotificationObserver;
import com.smartfood.service.DriverService;
import com.smartfood.service.NearestDriverStrategy;
import com.smartfood.service.OrderNotificationService;
import com.smartfood.service.OrderService;
import com.smartfood.service.RestaurantService;

import java.util.Scanner;

/**
 * Main entry point for the Smart Food Delivery application.
 *
 * Initializes the SQLite database, seeds demo data,
 * creates application services, and launches the
 * interactive console.
 *
 * Supports:
 * Sprint 1: Authentication and restaurant browsing.
 * Sprint 2: Order placement, validation and cancellation.
 * Sprint 3: Driver assignment, strategies and notifications.
 */
public class Main {

    public static void main(String[] args) {

        try {

            // ==========================================
            // STEP 1: CONNECT TO SQLITE
            // ==========================================

            DatabaseManager databaseManager =
                    new DatabaseManager();

            // ==========================================
            // STEP 2: INITIALIZE DATABASE TABLES
            // ==========================================

            DatabaseInitializer initializer =
                    new DatabaseInitializer(databaseManager);

            initializer.initialize();

            // ==========================================
            // STEP 3: CREATE SQLITE REPOSITORIES
            // ==========================================

            UserRepository userRepository =
                    new SqliteUserRepository(databaseManager);

            RestaurantRepository restaurantRepository =
                    new SqliteRestaurantRepository(databaseManager);

            OrderRepository orderRepository =
                    new SqliteOrderRepository(databaseManager);

            DriverStore driverRepository =
                    new SqliteDriverRepository(databaseManager);

            // ==========================================
            // STEP 4: PASSWORD HASHER
            // ==========================================

            PasswordHasher passwordHasher =
                    new PasswordHasher();

            // ==========================================
            // STEP 5: SEED USERS AND RESTAURANTS
            // ==========================================

            DataSeeder seeder = new DataSeeder(
                    userRepository,
                    restaurantRepository,
                    passwordHasher
            );

            seeder.seed();

            // ==========================================
            // STEP 6: SEED DEMO DRIVERS
            // ==========================================

            if (driverRepository.findAll().isEmpty()) {

                driverRepository.save(
                        new Driver(
                                "D1",
                                "Omar",
                                true,
                                2.5,
                                8
                        )
                );

                driverRepository.save(
                        new Driver(
                                "D2",
                                "Ahmad",
                                true,
                                4.0,
                                6
                        )
                );

                driverRepository.save(
                        new Driver(
                                "D3",
                                "Khaled",
                                true,
                                1.5,
                                12
                        )
                );
            }

            // ==========================================
            // STEP 7: CREATE AUTHENTICATION SERVICE
            // ==========================================

            AuthService authService = new AuthService(
                    userRepository,
                    passwordHasher
            );

            // ==========================================
            // STEP 8: CREATE RESTAURANT SERVICE
            // ==========================================

            RestaurantService restaurantService =
                    new RestaurantService(
                            restaurantRepository
                    );

            // ==========================================
            // STEP 9: CREATE SHARED NOTIFICATION SERVICE
            // ==========================================

            OrderNotificationService notificationService =
                    new OrderNotificationService();

            notificationService.addObserver(
                    new CustomerNotificationObserver()
            );

            // ==========================================
            // STEP 10: CREATE ORDER SERVICE
            // ==========================================

            OrderService orderService = new OrderService(
                    orderRepository,
                    notificationService
            );

            // ==========================================
            // STEP 11: CREATE DRIVER SERVICE
            // ==========================================

            DriverService driverService = new DriverService(
                    driverRepository,
                    new NearestDriverStrategy(),
                    notificationService
            );

            // ==========================================
            // STEP 12: START INTERACTIVE CONSOLE
            // ==========================================

            Scanner scanner = new Scanner(System.in);

            ConsoleMenu console = new ConsoleMenu(
                    authService,
                    restaurantService,
                    orderService,
                    driverService,
                    orderRepository,
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