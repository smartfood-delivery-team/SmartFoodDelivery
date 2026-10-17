package com.smartfood.presentation;

import com.smartfood.domain.*;
import com.smartfood.repository.*;
import com.smartfood.service.*;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        // =====================================
        // Sprint 1 - Authentication
        // =====================================

        UserRepository users = new UserRepository();

        users.save(new User("lana", "1234", Role.CUSTOMER));
        users.save(new User("admin", "admin", Role.MANAGER));

        AuthService auth = new AuthService(users);

        User u = auth.login("lana", "1234");

        System.out.println(
            "Logged in as: "
            + u.getUsername()
            + " (" + u.getRole() + ")"
        );

        // =====================================
        // Sprint 1 - Restaurant & Menu
        // =====================================

        RestaurantRepository repo = new RestaurantRepository();

        Restaurant pizza = new Restaurant("Pizza House", true);

        pizza.addMenuItem(
            new MenuItem("Margherita", 25.0, true)
        );

        pizza.addMenuItem(
            new MenuItem("Pepperoni", 30.0, false)
        );

        Restaurant closed = new Restaurant("Closed Cafe", false);

        repo.save(pizza);
        repo.save(closed);

        RestaurantService rs = new RestaurantService(repo);

        rs.getOpenRestaurants().forEach(
            r -> System.out.println("Open: " + r.getName())
        );

        rs.getAvailableMenu("Pizza House").forEach(
            i -> System.out.println(
                i.getName() + " - " + i.getPrice()
            )
        );

        // =====================================
        // Sprint 3 - Notification System
        // =====================================

        OrderNotificationService notificationService =
                new OrderNotificationService();

        CustomerNotificationObserver customerObserver =
                new CustomerNotificationObserver();

        notificationService.addObserver(customerObserver);

        // =====================================
        // Sprint 2 - Order Service
        // =====================================

        OrderRepository orderRepository =
                new OrderRepository();

        OrderService orderService =
                new OrderService(
                    orderRepository,
                    notificationService
                );

        // =====================================
        // Sprint 3 - Driver Repository
        // =====================================

        DriverRepository driverRepository =
                new DriverRepository();

        driverRepository.save(
            new Driver("D1", "Ahmad", true, 5.0, 15)
        );

        driverRepository.save(
            new Driver("D2", "Omar", true, 2.0, 20)
        );

        driverRepository.save(
            new Driver("D3", "Ali", true, 8.0, 10)
        );

        // =====================================
        // Sprint 3 - Driver Service
        // =====================================

        DriverService driverService =
                new DriverService(
                    driverRepository,
                    new NearestDriverStrategy(),
                    notificationService
                );

        // =====================================
        // Sprint 2 - Create an Order
        // =====================================

        MenuItem margherita =
                new MenuItem("Margherita", 25.0, true);

        OrderItem orderItem =
                new OrderItem(margherita, 2);

        // Create a valid order using the existing service.
        // The restaurant has no delivery-zone restriction by default.
        Order order = orderService.placeOrder(
            u,
            pizza,
            List.of(orderItem),
            "Nablus",
            pizza.getDeliveryZone()
        );

        System.out.println(
            "\nOrder created: " + order.getId()
        );

        System.out.println(
            "Order total: " + order.getTotal()
        );

        // =====================================
        // Sprint 3 - Assign Driver
        // =====================================

        Driver assignedDriver =
                driverService.assignDriver(order);

        System.out.println(
            "Assigned driver: "
            + assignedDriver.getName()
        );

        System.out.println(
            "Driver available: "
            + assignedDriver.isAvailable()
        );

        // =====================================
        // Sprint 3 - Order Status Notifications
        // =====================================

        orderService.updateOrderStatus(
            order.getId(),
            OrderStatus.CONFIRMED
        );

        orderService.updateOrderStatus(
            order.getId(),
            OrderStatus.PREPARING
        );

        orderService.updateOrderStatus(
            order.getId(),
            OrderStatus.READY
        );

        orderService.updateOrderStatus(
            order.getId(),
            OrderStatus.OUT_FOR_DELIVERY
        );

        orderService.updateOrderStatus(
            order.getId(),
            OrderStatus.DELIVERED
        );

        // =====================================
        // Sprint 3 - Release Driver
        // =====================================

        driverService.releaseDriver(order);

        System.out.println(
            "Driver available after delivery: "
            + assignedDriver.isAvailable()
        );

        // =====================================
        // Sprint 3 - Fastest Strategy Example
        // =====================================

        driverService.setSelectionStrategy(
            new FastestDriverStrategy()
        );

        System.out.println(
            "\nDriver selection strategy changed to Fastest."
        );

        System.out.println(
            "\nSmartFoodDelivery demo completed successfully!"
        );
    }
}