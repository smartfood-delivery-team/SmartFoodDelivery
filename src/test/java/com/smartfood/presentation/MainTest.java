package com.smartfood.presentation;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the SmartFoodDelivery main demo.
 *
 * Verifies that authentication, restaurant management,
 * order creation, driver assignment, notifications,
 * and delivery completion work together.
 */
class MainTest {

    @Test
    void testMainDemoRunsSuccessfully() {

        // Save the original console output.
        PrintStream originalOut = System.out;

        // Capture console output.
        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        try {

            System.setOut(
                new PrintStream(
                    outputStream,
                    true,
                    StandardCharsets.UTF_8
                )
            );

            // Run the complete application demo.
            assertDoesNotThrow(
                () -> Main.main(new String[0])
            );

        } finally {

            // Always restore console output.
            System.setOut(originalOut);
        }

        String output = outputStream.toString(
            StandardCharsets.UTF_8
        );

        // Sprint 1: Authentication
        assertTrue(
            output.contains("Logged in as: lana"),
            "Authentication should succeed"
        );

        // Sprint 1: Restaurant
        assertTrue(
            output.contains("Open: Pizza House"),
            "Open restaurant should be displayed"
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

        // Sprint 3: Order status notifications
        assertTrue(
            output.contains("OUT_FOR_DELIVERY"),
            "Customer should receive delivery notification"
        );

        assertTrue(
            output.contains("DELIVERED"),
            "Customer should receive delivered notification"
        );

        // Sprint 3: Driver release
        assertTrue(
            output.contains(
                "Driver available after delivery: true"
            ),
            "Driver should become available after delivery"
        );

        // Sprint 3: Strategy change
        assertTrue(
            output.contains(
                "Driver selection strategy changed to Fastest."
            ),
            "Driver selection strategy should change"
        );

        // Verify successful completion.
        assertTrue(
            output.contains(
                "SmartFoodDelivery demo completed successfully!"
            ),
            "Demo should finish successfully"
        );
    }
}