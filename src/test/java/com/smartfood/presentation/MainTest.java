package com.smartfood.presentation;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the SmartFoodDelivery application demo.
 *
 * Verifies authentication, restaurant browsing,
 * order creation, driver assignment, notifications,
 * and successful application execution.
 */
class MainTest {

    @Test
    void testMainDemoRunsSuccessfully() {

        PrintStream originalOut = System.out;

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        try (PrintStream testOut = new PrintStream(
                outputStream,
                true,
                StandardCharsets.UTF_8
        )) {

            System.setOut(testOut);

            // Run the current application demo
            assertDoesNotThrow(
                    () -> Main.main(new String[0]),
                    "Main application should run without exceptions"
            );

        } finally {
            // Restore original console output
            System.setOut(originalOut);
        }

        String output = outputStream.toString(StandardCharsets.UTF_8);

        // Sprint 1: Authentication
        assertTrue(
                output.contains("Logged in as: lana"),
                "Customer authentication should succeed"
        );

        // Sprint 1: Restaurant browsing
        assertTrue(
                output.contains("Open: Pizza House"),
                "Open restaurant should be displayed"
        );

        // Sprint 1: Menu browsing
        assertTrue(
                output.contains("Margherita - 25.0"),
                "Available menu item should be displayed"
        );

        assertFalse(
                output.contains("Pepperoni - 30.0"),
                "Unavailable menu item should not be displayed"
        );

        // Sprint 2: Order creation
        assertTrue(
                output.contains("Order created:"),
                "An order should be created"
        );

        assertTrue(
                output.contains("Order total: 50.0"),
                "Order total should equal 50.0"
        );

        // Sprint 3: Driver assignment
        assertTrue(
                output.contains("Assigned driver: Omar"),
                "Nearest available driver should be Omar"
        );

        assertTrue(
                output.contains("Driver available: false"),
                "Assigned driver should become busy"
        );

        // Sprint 3: Notifications
        assertTrue(
                output.contains("OUT_FOR_DELIVERY"),
                "Out-for-delivery notification should be generated"
        );

        assertTrue(
                output.contains("DELIVERED"),
                "Delivered notification should be generated"
        );

        // Sprint 3: Driver release
        assertTrue(
                output.contains("Driver available after delivery: true"),
                "Driver should become available after delivery"
        );

        // Sprint 3: Strategy change
        assertTrue(
                output.contains(
                        "Driver selection strategy changed to Fastest."
                ),
                "Driver selection strategy should change"
        );

        // Application completion
        assertTrue(
                output.contains(
                        "SmartFoodDelivery demo completed successfully!"
                ),
                "Application demo should finish successfully"
        );
    }
}