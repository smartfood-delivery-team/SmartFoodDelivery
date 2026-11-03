package com.smartfood.presentation;

import com.smartfood.domain.*;

import com.smartfood.repository.OrderRepository;

import com.smartfood.service.AuthService;
import com.smartfood.service.DriverService;
import com.smartfood.service.FastestDriverStrategy;
import com.smartfood.service.NearestDriverStrategy;
import com.smartfood.service.OrderService;
import com.smartfood.service.RestaurantService;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Interactive console for the Smart Food Delivery System.
 *
 * Supports customer and manager operations.
 */
public class ConsoleMenu {

    private final AuthService authService;
    private final RestaurantService restaurantService;
    private final OrderService orderService;
    private final DriverService driverService;
    private final OrderRepository orderRepository;
    private final Scanner scanner;

    private boolean running = true;

    // =====================================================
    // CONSTRUCTOR - SPRINT 1
    // =====================================================

    public ConsoleMenu(
            AuthService authService,
            RestaurantService restaurantService,
            Scanner scanner
    ) {

        this(
                authService,
                restaurantService,
                null,
                null,
                null,
                scanner
        );
    }

    // =====================================================
    // CONSTRUCTOR - SPRINT 2
    // =====================================================

    public ConsoleMenu(
            AuthService authService,
            RestaurantService restaurantService,
            OrderService orderService,
            Scanner scanner
    ) {

        this(
                authService,
                restaurantService,
                orderService,
                null,
                null,
                scanner
        );
    }

    // =====================================================
    // CONSTRUCTOR - SPRINT 3
    // =====================================================

    public ConsoleMenu(
            AuthService authService,
            RestaurantService restaurantService,
            OrderService orderService,
            DriverService driverService,
            OrderRepository orderRepository,
            Scanner scanner
    ) {

        this.authService = authService;
        this.restaurantService = restaurantService;
        this.orderService = orderService;
        this.driverService = driverService;
        this.orderRepository = orderRepository;
        this.scanner = scanner;
    }

    // =====================================================
    // APPLICATION START
    // =====================================================

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
                    System.out.println(
                            "Invalid choice. Please try again."
                    );
            }
        }

        System.out.println(
                "\nThank you for using Smart Food Delivery!"
        );
    }

    // =====================================================
    // WELCOME MENU
    // =====================================================

    private void showWelcomeMenu() {

        System.out.println();
        System.out.println(
                "=============================================="
        );
        System.out.println(
                "          SMART FOOD DELIVERY SYSTEM          "
        );
        System.out.println(
                "=============================================="
        );
        System.out.println(
                "               MAIN MENU                      "
        );
        System.out.println(
                "----------------------------------------------"
        );

        System.out.println("  1. Login");
        System.out.println("  0. Exit");

        System.out.println(
                "----------------------------------------------"
        );
    }

    // =====================================================
    // LOGIN
    // =====================================================

    private void handleLogin() {

        System.out.println();
        System.out.println(
                "=============================================="
        );
        System.out.println(
                "                  LOGIN                       "
        );
        System.out.println(
                "=============================================="
        );

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

            User user = authService.login(
                    username,
                    password
            );

            System.out.println();
            System.out.println("Login successful!");
            System.out.println(
                    "Welcome, " + user.getUsername() + "!"
            );
            System.out.println(
                    "Role: " + user.getRole()
            );

            if (user.getRole() == Role.CUSTOMER) {

                showCustomerMenu(user);

            } else if (user.getRole() == Role.MANAGER) {

                showManagerMenu(user);

            } else {

                System.out.println(
                        "Unsupported user role."
                );
            }

        } catch (IllegalArgumentException e) {

            System.out.println();
            System.out.println(
                    "Login failed: Invalid username or password."
            );
        }
    }

    // =====================================================
    // CUSTOMER DASHBOARD
    // =====================================================

    private void showCustomerMenu(User customer) {

        while (running) {

            System.out.println();
            System.out.println(
                    "=============================================="
            );
            System.out.println(
                    "              CUSTOMER DASHBOARD              "
            );
            System.out.println(
                    "=============================================="
            );

            System.out.println(
                    "Logged in as: " + customer.getUsername()
            );

            System.out.println(
                    "----------------------------------------------"
            );

            System.out.println("  1. Browse Open Restaurants");
            System.out.println("  2. View Restaurant Menu");
            System.out.println("  3. Logout");

            if (orderService != null) {

                System.out.println("  4. Place Order");
                System.out.println("  5. View My Orders");
                System.out.println("  6. Cancel Order");
            }

            System.out.println("  0. Exit");

            System.out.println(
                    "----------------------------------------------"
            );

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
                    System.out.println(
                            "Logged out successfully."
                    );
                    return;

                case 4:
                    if (orderService != null) {
                        placeOrder(customer);
                    } else {
                        System.out.println(
                                "Ordering is unavailable."
                        );
                    }
                    break;

                case 5:
                    if (orderService != null) {
                        showMyOrders(customer);
                    } else {
                        System.out.println(
                                "Order history is unavailable."
                        );
                    }
                    break;

                case 6:
                    if (orderService != null) {
                        cancelOrder(customer);
                    } else {
                        System.out.println(
                                "Order cancellation is unavailable."
                        );
                    }
                    break;

                case 0:
                    running = false;
                    return;

                default:
                    System.out.println(
                            "Invalid choice. Please try again."
                    );
            }
        }
    }

    // =====================================================
    // MANAGER DASHBOARD
    // =====================================================

    private void showManagerMenu(User manager) {

        while (running) {

            System.out.println();
            System.out.println(
                    "=============================================="
            );
            System.out.println(
                    "              MANAGER DASHBOARD               "
            );
            System.out.println(
                    "=============================================="
            );

            System.out.println(
                    "Logged in as: " + manager.getUsername()
            );

            System.out.println(
                    "----------------------------------------------"
            );

            System.out.println("  1. Browse Open Restaurants");
            System.out.println("  2. View Restaurant Menu");
            System.out.println("  3. Logout");

            if (driverService != null) {

                System.out.println("  4. View Available Drivers");
                System.out.println("  5. Assign Driver to Order");
                System.out.println(
                        "  6. Change Driver Selection Strategy"
                );
            }

            System.out.println("  0. Exit");

            System.out.println(
                    "----------------------------------------------"
            );

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
                    System.out.println(
                            "Logged out successfully."
                    );
                    return;

                case 4:
                    if (driverService != null) {
                        showAvailableDrivers();
                    } else {
                        System.out.println(
                                "Driver management is unavailable."
                        );
                    }
                    break;

                case 5:
                    if (driverService != null
                            && orderService != null
                            && orderRepository != null) {

                        assignDriverToOrder();

                    } else {

                        System.out.println(
                                "Driver assignment is unavailable."
                        );
                    }
                    break;

                case 6:
                    if (driverService != null) {
                        changeDriverStrategy();
                    } else {
                        System.out.println(
                                "Driver management is unavailable."
                        );
                    }
                    break;

                case 0:
                    running = false;
                    return;

                default:
                    System.out.println(
                            "Invalid choice. Please try again."
                    );
            }
        }
    }

    // =====================================================
    // VIEW OPEN RESTAURANTS
    // =====================================================

    private void showRestaurants() {

        List<Restaurant> restaurants =
                restaurantService.getOpenRestaurants();

        System.out.println();
        System.out.println(
                "=============================================="
        );
        System.out.println(
                "              OPEN RESTAURANTS                "
        );
        System.out.println(
                "=============================================="
        );

        if (restaurants.isEmpty()) {

            System.out.println(
                    "No open restaurants available."
            );
            return;
        }

        System.out.printf(
                "%-5s %-25s %-15s%n",
                "No.",
                "Restaurant",
                "Delivery Zone"
        );

        System.out.println(
                "----------------------------------------------"
        );

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

    // =====================================================
    // VIEW RESTAURANT MENU
    // =====================================================

    private void showRestaurantMenu() {

        List<Restaurant> restaurants =
                restaurantService.getOpenRestaurants();

        if (restaurants.isEmpty()) {

            System.out.println(
                    "No open restaurants available."
            );
            return;
        }

        showRestaurants();

        System.out.println();
        System.out.println("  0. Back");
        System.out.print("Select restaurant number: ");

        int choice = readChoice();

        if (!running || choice == 0) {
            return;
        }

        if (choice < 1 || choice > restaurants.size()) {

            System.out.println(
                    "Invalid restaurant number."
            );
            return;
        }

        Restaurant selectedRestaurant =
                restaurants.get(choice - 1);

        List<MenuItem> menu =
                restaurantService.getAvailableMenu(
                        selectedRestaurant.getName()
                );

        printMenu(selectedRestaurant, menu);
    }

    // =====================================================
    // PRINT RESTAURANT MENU
    // =====================================================

    private void printMenu(
            Restaurant restaurant,
            List<MenuItem> menu
    ) {

        System.out.println();
        System.out.println(
                "=============================================="
        );
        System.out.println(
                "             RESTAURANT MENU                  "
        );
        System.out.println(
                "=============================================="
        );

        System.out.println(
                "Restaurant: " + restaurant.getName()
        );

        System.out.println(
                "----------------------------------------------"
        );

        if (menu.isEmpty()) {

            System.out.println(
                    "No available menu items."
            );
            return;
        }

        System.out.printf(
                "%-5s %-25s %-10s%n",
                "No.",
                "Item",
                "Price (NIS)"
        );

        System.out.println(
                "----------------------------------------------"
        );

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

    // =====================================================
    // SPRINT 2 - PLACE ORDER
    // =====================================================

    private void placeOrder(User customer) {

        System.out.println();
        System.out.println(
                "=============================================="
        );
        System.out.println(
                "                PLACE ORDER                   "
        );
        System.out.println(
                "=============================================="
        );

        List<Restaurant> restaurants =
                restaurantService.getOpenRestaurants();

        if (restaurants.isEmpty()) {

            System.out.println(
                    "No open restaurants available."
            );
            return;
        }

        showRestaurants();

        System.out.println("  0. Back");
        System.out.print("Select restaurant number: ");

        int restaurantChoice = readChoice();

        if (!running || restaurantChoice == 0) {
            return;
        }

        if (restaurantChoice < 1
                || restaurantChoice > restaurants.size()) {

            System.out.println(
                    "Invalid restaurant number."
            );
            return;
        }

        Restaurant selectedRestaurant =
                restaurants.get(restaurantChoice - 1);

        List<MenuItem> menu =
                restaurantService.getAvailableMenu(
                        selectedRestaurant.getName()
                );

        if (menu.isEmpty()) {

            System.out.println(
                    "This restaurant has no available items."
            );
            return;
        }

        List<OrderItem> selectedItems =
                new ArrayList<>();

        while (running) {

            printMenu(selectedRestaurant, menu);

            System.out.println();
            System.out.println("  0. Finish selecting items");
            System.out.print("Select item number: ");

            int itemChoice = readChoice();

            if (!running) {
                return;
            }

            if (itemChoice == 0) {
                break;
            }

            if (itemChoice < 1 || itemChoice > menu.size()) {

                System.out.println(
                        "Invalid menu item number."
                );
                continue;
            }

            MenuItem selectedItem =
                    menu.get(itemChoice - 1);

            System.out.print("Enter quantity: ");

            int quantity = readChoice();

            if (!running) {
                return;
            }

            if (quantity <= 0) {

                System.out.println(
                        "Quantity must be greater than zero."
                );
                continue;
            }

            selectedItems.add(
                    new OrderItem(selectedItem, quantity)
            );

            System.out.printf(
                    "Added: %d x %s%n",
                    quantity,
                    selectedItem.getName()
            );
        }

        if (selectedItems.isEmpty()) {

            System.out.println(
                    "Cannot place an empty order."
            );
            return;
        }

        System.out.print("Enter delivery address: ");

        String deliveryAddress = readText();

        if (!running) {
            return;
        }

        System.out.print("Enter delivery zone: ");

        String deliveryZone = readText();

        if (!running) {
            return;
        }

        try {

            Order order = orderService.placeOrder(
                    customer,
                    selectedRestaurant,
                    selectedItems,
                    deliveryAddress,
                    deliveryZone
            );

            System.out.println();
            System.out.println(
                    "=============================================="
            );
            System.out.println(
                    "        ORDER PLACED SUCCESSFULLY             "
            );
            System.out.println(
                    "=============================================="
            );

            System.out.println(
                    "Order ID: " + order.getId()
            );

            System.out.println(
                    "Restaurant: "
                            + order.getRestaurant().getName()
            );

            System.out.println(
                    "Status: " + order.getStatus()
            );

            System.out.printf(
                    "Total: %.2f NIS%n",
                    order.getTotal()
            );

            System.out.println(
                    "Delivery Address: "
                            + order.getDeliveryAddress()
            );

        } catch (IllegalArgumentException
                 | IllegalStateException e) {

            System.out.println(
                    "Order rejected: " + e.getMessage()
            );
        }
    }

    // =====================================================
    // SPRINT 2 - VIEW CUSTOMER ORDERS
    // =====================================================

    private void showMyOrders(User customer) {

        System.out.println();
        System.out.println(
                "=============================================="
        );
        System.out.println(
                "                 MY ORDERS                    "
        );
        System.out.println(
                "=============================================="
        );

        try {

            List<Order> allOrders =
                    orderService.findAllOrders();

            boolean found = false;

            for (Order order : allOrders) {

                if (!order.getCustomer()
                        .getUsername()
                        .equals(customer.getUsername())) {

                    continue;
                }

                found = true;

                System.out.println(
                        "----------------------------------------------"
                );

                System.out.println(
                        "Order ID: " + order.getId()
                );

                System.out.println(
                        "Restaurant: "
                                + order.getRestaurant().getName()
                );

                System.out.println(
                        "Status: " + order.getStatus()
                );

                System.out.printf(
                        "Total: %.2f NIS%n",
                        order.getTotal()
                );

                System.out.println(
                        "Delivery Address: "
                                + order.getDeliveryAddress()
                );

                if (order.getAssignedDriver() != null) {

                    System.out.println(
                            "Assigned Driver: "
                                    + order.getAssignedDriver()
                                    .getName()
                    );

                } else {

                    System.out.println(
                            "Assigned Driver: Not assigned"
                    );
                }

                System.out.println("Items:");

                for (OrderItem item : order.getItems()) {

                    System.out.printf(
                            "  %d x %s - %.2f NIS%n",
                            item.getQuantity(),
                            item.getMenuItem().getName(),
                            item.getSubtotal()
                    );
                }
            }

            if (!found) {

                System.out.println(
                        "You have not placed any orders yet."
                );
            }

        } catch (IllegalStateException e) {

            System.out.println(
                    "Could not load orders: " + e.getMessage()
            );
        }
    }

    // =====================================================
    // SPRINT 2 - CANCEL ORDER
    // =====================================================

    private void cancelOrder(User customer) {

        System.out.println();
        System.out.println(
                "=============================================="
        );
        System.out.println(
                "                CANCEL ORDER                  "
        );
        System.out.println(
                "=============================================="
        );

        System.out.print("Enter Order ID: ");

        String orderId = readText();

        if (!running) {
            return;
        }

        if (orderId.isBlank()) {

            System.out.println(
                    "Order ID cannot be empty."
            );
            return;
        }

        try {

            Order order =
                    orderService.findOrderById(orderId);

            if (order == null) {

                System.out.println(
                        "Order not found."
                );
                return;
            }

            // Only the customer who owns the order
            // may cancel it through the console.
            if (!order.getCustomer()
                    .getUsername()
                    .equals(customer.getUsername())) {

                System.out.println(
                        "You cannot cancel this order."
                );
                return;
            }

            orderService.cancelOrder(orderId);

// Step 2: Release the assigned driver, if any
            if (driverService != null && order.getAssignedDriver() != null) {

                // The service requires the order to be CANCELLED.
                order.setStatus(OrderStatus.CANCELLED);

                driverService.releaseDriver(order);
            }

            System.out.println();
            System.out.println(
                    "Order cancelled successfully!"
            );

            System.out.println(
                    "Order ID: " + orderId
            );

            System.out.println(
                    "Status: CANCELLED"
            );

        } catch (IllegalArgumentException
                 | IllegalStateException e) {

            System.out.println(
                    "Cancellation failed: " + e.getMessage()
            );
        }
    }

    // =====================================================
    // SPRINT 3 - VIEW AVAILABLE DRIVERS
    // =====================================================

    private void showAvailableDrivers() {

        System.out.println();
        System.out.println(
                "=============================================="
        );
        System.out.println(
                "             AVAILABLE DRIVERS                "
        );
        System.out.println(
                "=============================================="
        );

        try {

            List<Driver> drivers =
                    driverService.getAvailableDrivers();

            if (drivers.isEmpty()) {

                System.out.println(
                        "No available drivers."
                );
                return;
            }

            System.out.printf(
                    "%-8s %-15s %-12s %-12s%n",
                    "ID",
                    "Name",
                    "Distance",
                    "ETA (min)"
            );

            System.out.println(
                    "----------------------------------------------"
            );

            for (Driver driver : drivers) {

                System.out.printf(
                        "%-8s %-15s %-12.2f %-12d%n",
                        driver.getId(),
                        driver.getName(),
                        driver.getDistance(),
                        driver.getEstimatedTime()
                );
            }

        } catch (IllegalStateException e) {

            System.out.println(
                    "Could not load drivers: " + e.getMessage()
            );
        }
    }

    // =====================================================
    // SPRINT 3 - ASSIGN DRIVER TO ORDER
    // =====================================================

    private void assignDriverToOrder() {

        System.out.println();
        System.out.println(
                "=============================================="
        );
        System.out.println(
                "              ASSIGN DRIVER                   "
        );
        System.out.println(
                "=============================================="
        );

        System.out.print("Enter Order ID: ");

        String orderId = readText();

        if (!running) {
            return;
        }

        if (orderId.isBlank()) {

            System.out.println(
                    "Order ID cannot be empty."
            );
            return;
        }

        try {

            Order order =
                    orderService.findOrderById(orderId);

            if (order == null) {

                System.out.println(
                        "Order not found."
                );
                return;
            }

            // Select and assign a driver using
            // the currently configured Strategy.
            Driver selectedDriver =
                    driverService.assignDriver(order);

            // Persist the order-driver relationship.
            orderRepository.save(order);

            System.out.println();
            System.out.println(
                    "=============================================="
            );
            System.out.println(
                    "        DRIVER ASSIGNED SUCCESSFULLY          "
            );
            System.out.println(
                    "=============================================="
            );

            System.out.println(
                    "Order ID: " + order.getId()
            );

            System.out.println(
                    "Driver ID: " + selectedDriver.getId()
            );

            System.out.println(
                    "Driver Name: " + selectedDriver.getName()
            );

            System.out.printf(
                    "Distance: %.2f km%n",
                    selectedDriver.getDistance()
            );

            System.out.println(
                    "Estimated Arrival: "
                            + selectedDriver.getEstimatedTime()
                            + " minutes"
            );

            System.out.println(
                    "Driver Status: BUSY"
            );

        } catch (IllegalArgumentException
                 | IllegalStateException e) {

            System.out.println(
                    "Driver assignment failed: " + e.getMessage()
            );
        }
    }

    // =====================================================
    // SPRINT 3 - CHANGE DRIVER SELECTION STRATEGY
    // =====================================================

    private void changeDriverStrategy() {

        System.out.println();
        System.out.println(
                "=============================================="
        );
        System.out.println(
                "          DRIVER SELECTION STRATEGY           "
        );
        System.out.println(
                "=============================================="
        );

        System.out.println("  1. Nearest Driver");
        System.out.println("  2. Fastest Driver");
        System.out.println("  0. Back");

        int choice = readChoice();

        if (!running || choice == 0) {
            return;
        }

        switch (choice) {

            case 1:

                driverService.setSelectionStrategy(
                        new NearestDriverStrategy()
                );

                System.out.println(
                        "Strategy changed to Nearest Driver."
                );
                break;

            case 2:

                driverService.setSelectionStrategy(
                        new FastestDriverStrategy()
                );

                System.out.println(
                        "Strategy changed to Fastest Driver."
                );
                break;

            default:

                System.out.println(
                        "Invalid strategy selection."
                );
        }
    }

    // =====================================================
    // INPUT VALIDATION
    // =====================================================

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

    // =====================================================
    // READ TEXT
    // =====================================================

    private String readText() {

        if (!scanner.hasNextLine()) {

            running = false;
            return "";
        }

        return scanner.nextLine().trim();
    }
}