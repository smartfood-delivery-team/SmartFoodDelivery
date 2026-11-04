package com.smartfood.presentation;

import com.smartfood.domain.Driver;
import com.smartfood.domain.MenuItem;
import com.smartfood.domain.Order;
import com.smartfood.domain.OrderType;
import com.smartfood.domain.ScheduledOrder;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import com.smartfood.domain.OrderItem;
import com.smartfood.domain.OrderStatus;
import com.smartfood.domain.PaymentStatus;
import com.smartfood.domain.Restaurant;
import com.smartfood.domain.Role;
import com.smartfood.domain.User;

import com.smartfood.pricing.ExpressPricingStrategy;
import com.smartfood.pricing.NormalPricingStrategy;
import com.smartfood.pricing.PeakHourPricingStrategy;
import com.smartfood.pricing.PromotionalPricingStrategy;

import com.smartfood.repository.OrderRepository;

import com.smartfood.service.AuthService;
import com.smartfood.service.DriverService;
import com.smartfood.service.FastestDriverStrategy;
import com.smartfood.service.NearestDriverStrategy;
import com.smartfood.service.OrderService;
import com.smartfood.service.PaymentService;
import com.smartfood.service.RestaurantService;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Interactive console for the Smart Food Delivery System.
 *
 * Sprint 1: Authentication and restaurant browsing.
 * Sprint 2: Order placement and cancellation.
 * Sprint 3: Driver assignment and selection strategies.
 * Sprint 4: Order processing, delivery fees, pricing and payments.
 */
public class ConsoleMenu {

    private final AuthService authService;
    private final RestaurantService restaurantService;
    private final OrderService orderService;
    private final DriverService driverService;
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final Scanner scanner;

    private boolean running = true;

    // =====================================================
    // CONSTRUCTORS
    // =====================================================

    // Sprint 1 constructor
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
                null,
                scanner
        );
    }

    // Sprint 2 constructor
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
                null,
                scanner
        );
    }

    // Sprint 3 constructor
    public ConsoleMenu(
            AuthService authService,
            RestaurantService restaurantService,
            OrderService orderService,
            DriverService driverService,
            OrderRepository orderRepository,
            Scanner scanner
    ) {
        this(
                authService,
                restaurantService,
                orderService,
                driverService,
                orderRepository,
                null,
                scanner
        );
    }

    // Sprint 4 constructor
    public ConsoleMenu(
            AuthService authService,
            RestaurantService restaurantService,
            OrderService orderService,
            DriverService driverService,
            OrderRepository orderRepository,
            PaymentService paymentService,
            Scanner scanner
    ) {
        this.authService = authService;
        this.restaurantService = restaurantService;
        this.orderService = orderService;
        this.driverService = driverService;
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
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
        System.out.println("==============================================");
        System.out.println("          SMART FOOD DELIVERY SYSTEM          ");
        System.out.println("==============================================");
        System.out.println("               MAIN MENU                      ");
        System.out.println("----------------------------------------------");
        System.out.println("  1. Login");
        System.out.println("  0. Exit");
        System.out.println("----------------------------------------------");
    }

    // =====================================================
    // LOGIN
    // =====================================================

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
            System.out.println("==============================================");
            System.out.println("              CUSTOMER DASHBOARD              ");
            System.out.println("==============================================");
            System.out.println(
                    "Logged in as: " + customer.getUsername()
            );
            System.out.println("----------------------------------------------");

            System.out.println("  1. Browse Open Restaurants");
            System.out.println("  2. View Restaurant Menu");
            System.out.println("  3. Logout");

            if (orderService != null) {
                System.out.println("  4. Place Order");
                System.out.println("  5. View My Orders");
                System.out.println("  6. Cancel Order");
            }

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

                case 4:
                    if (orderService != null) {
                        placeOrder(customer);
                    } else {
                        System.out.println("Ordering is unavailable.");
                    }
                    break;

                case 5:
                    if (orderService != null) {
                        showMyOrders(customer);
                    } else {
                        System.out.println("Order history is unavailable.");
                    }
                    break;

                case 6:
                    if (orderService != null) {
                        cancelOrder(customer);
                    } else {
                        System.out.println("Cancellation is unavailable.");
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
            System.out.println("==============================================");
            System.out.println("              MANAGER DASHBOARD               ");
            System.out.println("==============================================");
            System.out.println(
                    "Logged in as: " + manager.getUsername()
            );
            System.out.println("----------------------------------------------");

            System.out.println("  1. Browse Open Restaurants");
            System.out.println("  2. View Restaurant Menu");
            System.out.println("  3. Logout");

            if (driverService != null) {
                System.out.println("  4. View Available Drivers");
                System.out.println("  5. Assign Driver to Order");
                System.out.println("  6. Change Driver Selection Strategy");
            }

            if (paymentService != null
                    && orderService != null
                    && orderRepository != null) {

                System.out.println("  7. Update Order Status");
                System.out.println("  8. Calculate Delivery Fee");
                System.out.println("  9. Change Pricing Strategy");
                System.out.println(" 10. Process Cash Payment");
            }

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

                case 7:
                    if (paymentService != null
                            && orderService != null) {

                        updateOrderStatus();

                    } else {
                        System.out.println(
                                "Order management is unavailable."
                        );
                    }
                    break;

                case 8:
                    if (paymentService != null
                            && orderService != null
                            && orderRepository != null) {

                        calculateDeliveryFee();

                    } else {
                        System.out.println(
                                "Delivery fee management is unavailable."
                        );
                    }
                    break;

                case 9:
                    if (paymentService != null
                            && orderService != null
                            && orderRepository != null) {

                        changeOrderPricingStrategy();

                    } else {
                        System.out.println(
                                "Pricing management is unavailable."
                        );
                    }
                    break;

                case 10:
                    if (paymentService != null
                            && orderService != null
                            && orderRepository != null) {

                        processCashPayment();

                    } else {
                        System.out.println(
                                "Payment processing is unavailable."
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
    // SPRINT 1 - VIEW OPEN RESTAURANTS
    // =====================================================

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

    // =====================================================
    // SPRINT 1 - VIEW RESTAURANT MENU
    // =====================================================

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
        System.out.println("==============================================");
        System.out.println("             RESTAURANT MENU                  ");
        System.out.println("==============================================");
        System.out.println(
                "Restaurant: " + restaurant.getName()
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

    // =====================================================
    // SPRINT 2 - PLACE ORDER
    // =====================================================

    private void placeOrder(User customer) {

        System.out.println();
        System.out.println("==============================================");
        System.out.println("                PLACE ORDER                   ");
        System.out.println("==============================================");

        List<Restaurant> restaurants =
                restaurantService.getOpenRestaurants();

        if (restaurants.isEmpty()) {
            System.out.println("No open restaurants available.");
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

            System.out.println("Invalid restaurant number.");
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

        List<OrderItem> selectedItems = new ArrayList<>();

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
                System.out.println("Invalid menu item number.");
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
            System.out.println("Cannot place an empty order.");
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

        System.out.println("Order type: 1 Standard, 2 Express, 3 Scheduled, 0 Back");
        int typeChoice = readChoice();
        if (!running || typeChoice == 0) return;
        OrderType type;
        switch (typeChoice) {
            case 1 -> type = OrderType.STANDARD;
            case 2 -> type = OrderType.EXPRESS;
            case 3 -> type = OrderType.SCHEDULED;
            default -> { System.out.println("Invalid order type"); return; }
        }
        LocalDateTime scheduledFor = null;
        if (type == OrderType.SCHEDULED) {
            System.out.print("Delivery time (YYYY-MM-DDTHH:MM): ");
            String value = readText();
            if (!running) return;
            try { scheduledFor = LocalDateTime.parse(value); }
            catch (DateTimeParseException ex) {
                System.out.println("Invalid date/time format"); return;
            }
        }

        try {
            Order order = orderService.placeOrder(customer,selectedRestaurant,selectedItems,
                    deliveryAddress,deliveryZone,type,scheduledFor);

            System.out.println();
            System.out.println("==============================================");
            System.out.println("        ORDER PLACED SUCCESSFULLY             ");
            System.out.println("==============================================");

            System.out.println("Order ID: " + order.getId());
            System.out.println("Order Type: " + order.getOrderType());
            System.out.println(
                    "Restaurant: " + order.getRestaurant().getName()
            );
            System.out.println("Status: " + order.getStatus());

            System.out.printf(
                    "Total: %.2f NIS%n",
                    order.getTotal()
            );

            System.out.println(
                    "Delivery Address: " + order.getDeliveryAddress()
            );

        } catch (IllegalArgumentException | IllegalStateException e) {

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
        System.out.println("==============================================");
        System.out.println("                 MY ORDERS                    ");
        System.out.println("==============================================");

        try {

            List<Order> allOrders =
                    orderService.findAllOrders();

            boolean found = false;

            for (Order order : allOrders) {

                // Show only orders owned by this customer.
                if (!order.getCustomer().getUsername()
                        .equals(customer.getUsername())) {

                    continue;
                }

                found = true;

                System.out.println("----------------------------------------------");
                System.out.println("Order ID: " + order.getId());
                System.out.println("Order Type: " + order.getOrderType());
                if (order instanceof ScheduledOrder scheduled)
                    System.out.println("Scheduled For: " + scheduled.getScheduledFor());

                System.out.println(
                        "Restaurant: " + order.getRestaurant().getName()
                );

                System.out.println(
                        "Status: " + order.getStatus()
                );

                System.out.printf(
                        "Food Total: %.2f NIS%n",
                        order.getTotal()
                );

                System.out.printf(
                        "Delivery Fee: %.2f NIS%n",
                        order.getDeliveryFee()
                );

                System.out.printf(
                        "Final Total: %.2f NIS%n",
                        order.getDynamicFinalTotal()
                );

                System.out.println(
                        "Payment Status: " + order.getPaymentStatus()
                );

                System.out.println(
                        "Delivery Address: " + order.getDeliveryAddress()
                );

                if (order.getAssignedDriver() != null) {

                    System.out.println(
                            "Assigned Driver: "
                                    + order.getAssignedDriver().getName()
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
    // SPRINT 2 + 3 - CANCEL ORDER AND RELEASE DRIVER
    // =====================================================

    private void cancelOrder(User customer) {

        System.out.println();
        System.out.println("==============================================");
        System.out.println("                CANCEL ORDER                  ");
        System.out.println("==============================================");

        System.out.print("Enter Order ID: ");
        String orderId = readText();

        if (!running) {
            return;
        }

        if (orderId.isBlank()) {
            System.out.println("Order ID cannot be empty.");
            return;
        }

        try {

            Order order =
                    orderService.findOrderById(orderId);

            if (order == null) {
                System.out.println("Order not found.");
                return;
            }

            if (!order.getCustomer().getUsername()
                    .equals(customer.getUsername())) {

                System.out.println(
                        "You cannot cancel this order."
                );
                return;
            }

            // Cancel through OrderService.
            orderService.cancelOrder(orderId);

            // Release the driver, if one was assigned.
            if (driverService != null
                    && order.getAssignedDriver() != null) {

                order.setStatus(OrderStatus.CANCELLED);

                driverService.releaseDriver(order);

                System.out.println(
                        "Assigned driver released successfully."
                );
            }

            System.out.println();
            System.out.println("Order cancelled successfully!");
            System.out.println("Order ID: " + orderId);
            System.out.println("Status: CANCELLED");

        } catch (IllegalArgumentException | IllegalStateException e) {

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
        System.out.println("==============================================");
        System.out.println("             AVAILABLE DRIVERS                ");
        System.out.println("==============================================");

        try {

            List<Driver> drivers =
                    driverService.getAvailableDrivers();

            if (drivers.isEmpty()) {
                System.out.println("No available drivers.");
                return;
            }

            System.out.printf(
                    "%-8s %-15s %-12s %-12s%n",
                    "ID",
                    "Name",
                    "Distance",
                    "ETA (min)"
            );

            System.out.println("----------------------------------------------");

            for (Driver driver : drivers) {

                System.out.printf(
                        "%-8s %-15s %-12.2f %-12d%n",
                        driver.getId(),
                        driver.getName(),
                        driver.getDistance(),
                        driver.getEstimatedTime()
                );
                System.out.println("         Type: " + driver.getDriverType());
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
        System.out.println("==============================================");
        System.out.println("              ASSIGN DRIVER                   ");
        System.out.println("==============================================");

        System.out.print("Enter Order ID: ");
        String orderId = readText();

        if (!running) {
            return;
        }

        try {

            Order order =
                    orderService.findOrderById(orderId);

            if (order == null) {
                System.out.println("Order not found.");
                return;
            }

            Driver selectedDriver =
                    driverService.assignDriver(order);

            // Save the assigned driver ID in SQLite.
            orderRepository.save(order);

            System.out.println();
            System.out.println(
                    "Driver assigned successfully!"
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

        } catch (IllegalArgumentException | IllegalStateException e) {

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
        System.out.println("==============================================");
        System.out.println("          DRIVER SELECTION STRATEGY           ");
        System.out.println("==============================================");

        System.out.println("1. Nearest Driver");
        System.out.println("2. Fastest Driver");
        System.out.println("0. Back");

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
    // SPRINT 4 - UPDATE ORDER STATUS
    // =====================================================

    private void updateOrderStatus() {

        System.out.println();
        System.out.println("==============================================");
        System.out.println("           UPDATE ORDER STATUS                ");
        System.out.println("==============================================");

        System.out.print("Enter Order ID: ");
        String orderId = readText();

        if (!running) {
            return;
        }

        try {

            Order order =
                    orderService.findOrderById(orderId);

            if (order == null) {
                System.out.println("Order not found.");
                return;
            }

            System.out.println(
                    "Current Status: " + order.getStatus()
            );

            System.out.println();
            System.out.println("1. CONFIRMED");
            System.out.println("2. PREPARING");
            System.out.println("3. READY");
            System.out.println("4. OUT_FOR_DELIVERY");
            System.out.println("5. DELIVERED");
            System.out.println("0. Back");

            int choice = readChoice();

            if (!running || choice == 0) {
                return;
            }

            OrderStatus newStatus;

            switch (choice) {

                case 1:
                    newStatus = OrderStatus.CONFIRMED;
                    break;

                case 2:
                    newStatus = OrderStatus.PREPARING;
                    break;

                case 3:
                    newStatus = OrderStatus.READY;
                    break;

                case 4:
                    newStatus = OrderStatus.OUT_FOR_DELIVERY;
                    break;

                case 5:
                    newStatus = OrderStatus.DELIVERED;
                    break;

                default:
                    System.out.println(
                            "Invalid status selection."
                    );
                    return;
            }

            // OrderService validates allowed transitions.
            orderService.updateOrderStatus(
                    orderId,
                    newStatus
            );

            System.out.println(
                    "Order status updated to: " + newStatus
            );

            // Release driver after delivery.
            if (newStatus == OrderStatus.DELIVERED
                    && driverService != null) {

                Order deliveredOrder =
                        orderService.findOrderById(orderId);

                if (deliveredOrder != null
                        && deliveredOrder.getAssignedDriver() != null) {

                    driverService.releaseDriver(deliveredOrder);

                    System.out.println(
                            "Assigned driver is now available."
                    );
                }
            }

        } catch (IllegalArgumentException | IllegalStateException e) {

            System.out.println(
                    "Status update failed: " + e.getMessage()
            );
        }
    }

    // =====================================================
    // SPRINT 4 - CALCULATE DELIVERY FEE
    // =====================================================

    private void calculateDeliveryFee() {

        System.out.println();
        System.out.println("==============================================");
        System.out.println("          CALCULATE DELIVERY FEE              ");
        System.out.println("==============================================");

        System.out.print("Enter Order ID: ");
        String orderId = readText();

        if (!running) {
            return;
        }

        try {

            Order order =
                    orderService.findOrderById(orderId);

            if (order == null) {
                System.out.println("Order not found.");
                return;
            }

            if (order.getStatus() == OrderStatus.CANCELLED
                    || order.getStatus() == OrderStatus.DELIVERED) {

                System.out.println(
                        "Cannot change delivery fee for a closed order."
                );
                return;
            }

            if (order.getPaymentStatus() == PaymentStatus.PAID) {

                System.out.println(
                        "Cannot change delivery fee after payment."
                );
                return;
            }

            System.out.print("Enter delivery distance (km): ");
            String input = readText();

            if (!running) {
                return;
            }

            double distance = Double.parseDouble(input);

            if (!Double.isFinite(distance) || distance < 0) {

                System.out.println(
                        "Distance must be finite and non-negative."
                );
                return;
            }

            // Calculate fee using Order domain logic.
            order.calculateDeliveryFee(distance);

            // Persist delivery fee and distance.
            orderRepository.save(order);

            System.out.println();
            System.out.printf(
                    "Food Total: %.2f NIS%n",
                    order.getTotal()
            );

            System.out.printf(
                    "Delivery Distance: %.2f km%n",
                    order.getDeliveryDistanceKm()
            );

            System.out.printf(
                    "Delivery Fee: %.2f NIS%n",
                    order.getDeliveryFee()
            );

            System.out.printf(
                    "Final Total: %.2f NIS%n",
                    order.getDynamicFinalTotal()
            );

            System.out.println(
                    "Delivery fee saved successfully."
            );

        } catch (IllegalArgumentException | IllegalStateException e) {

            System.out.println(
                    "Delivery fee calculation failed: " + e.getMessage()
            );
        }
    }

    // =====================================================
    // SPRINT 4 - CHANGE PRICING STRATEGY
    // =====================================================

    private void changeOrderPricingStrategy() {

        System.out.println();
        System.out.println("==============================================");
        System.out.println("            PRICING STRATEGY                  ");
        System.out.println("==============================================");

        System.out.print("Enter Order ID: ");
        String orderId = readText();

        if (!running) {
            return;
        }

        try {

            Order order =
                    orderService.findOrderById(orderId);

            if (order == null) {
                System.out.println("Order not found.");
                return;
            }

            if (order.getStatus() == OrderStatus.CANCELLED
                    || order.getStatus() == OrderStatus.DELIVERED) {

                System.out.println(
                        "Cannot change pricing for a closed order."
                );
                return;
            }

            if (order.getPaymentStatus() == PaymentStatus.PAID) {

                System.out.println(
                        "Cannot change pricing after payment."
                );
                return;
            }

            System.out.println();
            System.out.println("1. Normal Pricing");
            System.out.println("2. Express Pricing (+15%)");
            System.out.println("3. Peak Hour Pricing (+20%)");
            System.out.println("4. Promotional Pricing (-10%)");
            System.out.println("0. Back");

            int choice = readChoice();

            if (!running || choice == 0) {
                return;
            }

            switch (choice) {

                case 1:
                    order.setPricingStrategy(
                            new NormalPricingStrategy()
                    );
                    break;

                case 2:
                    order.setPricingStrategy(
                            new ExpressPricingStrategy()
                    );
                    break;

                case 3:
                    order.setPricingStrategy(
                            new PeakHourPricingStrategy()
                    );
                    break;

                case 4:
                    order.setPricingStrategy(
                            new PromotionalPricingStrategy()
                    );
                    break;

                default:
                    System.out.println(
                            "Invalid pricing strategy."
                    );
                    return;
            }

            // Save selected strategy in SQLite.
            orderRepository.save(order);

            System.out.println();
            System.out.println(
                    "Pricing strategy updated successfully."
            );

            System.out.printf(
                    "Food Total: %.2f NIS%n",
                    order.getTotal()
            );

            System.out.printf(
                    "Delivery Fee: %.2f NIS%n",
                    order.getDeliveryFee()
            );

            System.out.printf(
                    "Adjusted Total: %.2f NIS%n",
                    order.getDynamicFinalTotal()
            );

        } catch (IllegalArgumentException | IllegalStateException e) {

            System.out.println(
                    "Pricing update failed: " + e.getMessage()
            );
        }
    }

    // =====================================================
    // SPRINT 4 - PROCESS CASH PAYMENT
    // =====================================================

    private void processCashPayment() {

        System.out.println();
        System.out.println("==============================================");
        System.out.println("            PROCESS CASH PAYMENT              ");
        System.out.println("==============================================");

        System.out.print("Enter Order ID: ");
        String orderId = readText();

        if (!running) {
            return;
        }

        try {

            Order order =
                    orderService.findOrderById(orderId);

            if (order == null) {
                System.out.println("Order not found.");
                return;
            }

            System.out.println(
                    "Current Payment Status: "
                            + order.getPaymentStatus()
            );

            if (order.getStatus() == OrderStatus.CANCELLED) {

                System.out.println(
                        "Cannot process payment for a cancelled order."
                );
                return;
            }

            if (order.getPaymentStatus() == PaymentStatus.PAID) {

                System.out.println(
                        "This order has already been paid."
                );
                return;
            }

            double requiredAmount =
                    order.getDynamicFinalTotal();

            System.out.printf(
                    "Amount Due: %.2f NIS%n",
                    requiredAmount
            );

            System.out.print("Enter cash amount: ");
            String input = readText();

            if (!running) {
                return;
            }

            double receivedAmount =
                    Double.parseDouble(input);

            // Validate cash payment using PaymentService.
            PaymentStatus result =
                    paymentService.processCashPayment(
                            order,
                            receivedAmount
                    );

            // Save payment result in SQLite.
            orderRepository.save(order);

            if (result == PaymentStatus.PAID) {

                System.out.println();
                System.out.println(
                        "Payment completed successfully!"
                );

            } else {

                System.out.println();
                System.out.println(
                        "Payment failed: Incorrect payment amount."
                );
            }

            System.out.println(
                    "Payment Status: " + result
            );

        } catch (IllegalArgumentException | IllegalStateException e) {

            System.out.println(
                    "Payment processing failed: " + e.getMessage()
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
    // READ TEXT INPUT
    // =====================================================

    private String readText() {

        if (!scanner.hasNextLine()) {
            running = false;
            return "";
        }

        return scanner.nextLine().trim();
    }
}