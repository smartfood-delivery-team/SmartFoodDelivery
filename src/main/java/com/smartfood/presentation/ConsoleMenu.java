package com.smartfood.presentation;

import com.smartfood.domain.MenuItem;
import com.smartfood.domain.Restaurant;
import com.smartfood.domain.Role;
import com.smartfood.domain.User;

import com.smartfood.service.AuthService;
import com.smartfood.service.RestaurantService;

import java.util.List;
import java.util.Scanner;

public class ConsoleMenu {

    private final AuthService authService;
    private final RestaurantService restaurantService;
    private final Scanner scanner;

    private boolean running = true;

    public ConsoleMenu(
            AuthService authService,
            RestaurantService restaurantService,
            Scanner scanner
    ) {
        this.authService = authService;
        this.restaurantService = restaurantService;
        this.scanner = scanner;
    }

    // ==========================================
    // APPLICATION START
    // ==========================================

    public void start() {

        while (running) {

            showWelcomeMenu();

            int choice = readChoice();

            if (!running) {
                break;
            }

            switch (choice) {

                case 1:
                    handleLogin();
                    break;

                case 0:
                    running = false;
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }

        System.out.println("\nThank you for using Smart Food Delivery!");
    }

    // ==========================================
    // WELCOME MENU
    // ==========================================

    private void showWelcomeMenu() {

        System.out.println();
        System.out.println("==============================================");
        System.out.println("          SMART FOOD DELIVERY SYSTEM          ");
        System.out.println("==============================================");
        System.out.println("               MAIN MENU                      ");
        System.out.println("----------------------------------------------");
        System.out.println("  1. Login");
        System.out.println("  0. Exit");
        System.out.println("----------------------------------------------");
    }

    // ==========================================
    // LOGIN
    // ==========================================

    private void handleLogin() {

        System.out.println();
        System.out.println("==============================================");
        System.out.println("                  LOGIN                       ");
        System.out.println("==============================================");

        System.out.print("Username: ");

        if (!scanner.hasNextLine()) {
            running = false;
            return;
        }

        String username = scanner.nextLine().trim();

        System.out.print("Password: ");

        if (!scanner.hasNextLine()) {
            running = false;
            return;
        }

        String password = scanner.nextLine();

        try {

            User user = authService.login(username, password);

            System.out.println();
            System.out.println("Login successful!");
            System.out.println("Welcome, " + user.getUsername() + "!");
            System.out.println("Role: " + user.getRole());

            if (user.getRole() == Role.CUSTOMER) {

                showCustomerMenu(user);

            } else if (user.getRole() == Role.MANAGER) {

                showManagerMenu(user);

            } else {

                System.out.println("Unsupported user role.");
            }

        } catch (IllegalArgumentException e) {

            System.out.println();
            System.out.println("Login failed: Invalid username or password.");
        }
    }

    // ==========================================
    // CUSTOMER DASHBOARD
    // ==========================================

    private void showCustomerMenu(User customer) {

        while (running) {

            System.out.println();
            System.out.println("==============================================");
            System.out.println("              CUSTOMER DASHBOARD              ");
            System.out.println("==============================================");
            System.out.println("Logged in as: " + customer.getUsername());
            System.out.println("----------------------------------------------");
            System.out.println("  1. Browse Open Restaurants");
            System.out.println("  2. View Restaurant Menu");
            System.out.println("  3. Logout");
            System.out.println("  0. Exit");
            System.out.println("----------------------------------------------");

            int choice = readChoice();

            if (!running) {
                return;
            }

            switch (choice) {

                case 1:
                    showRestaurants();
                    break;

                case 2:
                    showRestaurantMenu();
                    break;

                case 3:
                    System.out.println("Logged out successfully.");
                    return;

                case 0:
                    running = false;
                    return;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // ==========================================
    // MANAGER DASHBOARD
    // ==========================================

    private void showManagerMenu(User manager) {

        while (running) {

            System.out.println();
            System.out.println("==============================================");
            System.out.println("              MANAGER DASHBOARD               ");
            System.out.println("==============================================");
            System.out.println("Logged in as: " + manager.getUsername());
            System.out.println("----------------------------------------------");
            System.out.println("  1. Browse Open Restaurants");
            System.out.println("  2. View Restaurant Menu");
            System.out.println("  3. Logout");
            System.out.println("  0. Exit");
            System.out.println("----------------------------------------------");

            int choice = readChoice();

            if (!running) {
                return;
            }

            switch (choice) {

                case 1:
                    showRestaurants();
                    break;

                case 2:
                    showRestaurantMenu();
                    break;

                case 3:
                    System.out.println("Logged out successfully.");
                    return;

                case 0:
                    running = false;
                    return;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // ==========================================
    // VIEW OPEN RESTAURANTS
    // ==========================================

    private void showRestaurants() {

        List<Restaurant> restaurants =
                restaurantService.getOpenRestaurants();

        System.out.println();
        System.out.println("==============================================");
        System.out.println("              OPEN RESTAURANTS                ");
        System.out.println("==============================================");

        if (restaurants.isEmpty()) {

            System.out.println("No open restaurants available.");
            return;
        }

        System.out.printf(
                "%-5s %-25s %-15s%n",
                "No.",
                "Restaurant",
                "Delivery Zone"
        );

        System.out.println("----------------------------------------------");

        for (int i = 0; i < restaurants.size(); i++) {

            Restaurant restaurant = restaurants.get(i);

            System.out.printf(
                    "%-5d %-25s %-15s%n",
                    i + 1,
                    restaurant.getName(),
                    restaurant.getDeliveryZone()
            );
        }
    }

    // ==========================================
    // VIEW RESTAURANT MENU
    // ==========================================

    private void showRestaurantMenu() {

        List<Restaurant> restaurants =
                restaurantService.getOpenRestaurants();

        if (restaurants.isEmpty()) {

            System.out.println("No open restaurants available.");
            return;
        }

        showRestaurants();

        System.out.println();
        System.out.println("  0. Back");
        System.out.println("----------------------------------------------");
        System.out.print("Select restaurant number: ");

        int choice = readChoice();

        if (!running || choice == 0) {
            return;
        }

        if (choice < 1 || choice > restaurants.size()) {

            System.out.println("Invalid restaurant number.");
            return;
        }

        Restaurant selectedRestaurant =
                restaurants.get(choice - 1);

        List<MenuItem> menu =
                restaurantService.getAvailableMenu(
                        selectedRestaurant.getName()
                );

        System.out.println();
        System.out.println("==============================================");
        System.out.println("             RESTAURANT MENU                  ");
        System.out.println("==============================================");
        System.out.println(
                "Restaurant: " + selectedRestaurant.getName()
        );
        System.out.println("----------------------------------------------");

        if (menu.isEmpty()) {

            System.out.println("No available menu items.");
            return;
        }

        System.out.printf(
                "%-5s %-25s %-10s%n",
                "No.",
                "Item",
                "Price (NIS)"
        );

        System.out.println("----------------------------------------------");

        for (int i = 0; i < menu.size(); i++) {

            MenuItem item = menu.get(i);

            System.out.printf(
                    "%-5d %-25s %-10.2f%n",
                    i + 1,
                    item.getName(),
                    item.getPrice()
            );
        }
    }

    // ==========================================
    // INPUT VALIDATION
    // ==========================================

    private int readChoice() {

        System.out.print("Enter your choice: ");

        if (!scanner.hasNextLine()) {
            running = false;
            return -1;
        }

        String input = scanner.nextLine().trim();

        try {

            return Integer.parseInt(input);

        } catch (NumberFormatException e) {

            System.out.println(
                    "Invalid input. Please enter a number."
            );

            return -1;
        }
    }
}