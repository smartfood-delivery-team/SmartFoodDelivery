package com.smartfood.service;

import com.smartfood.domain.Driver;
import com.smartfood.domain.Order;
import com.smartfood.domain.OrderStatus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests customer notifications for order status changes
 * and driver assignments.
 */
class CustomerNotificationObserverTest {

    private CustomerNotificationObserver observer;
    private Order order;

    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {

        observer = new CustomerNotificationObserver();

        order = new Order(
            "ORD-300",
            null,
            null,
            List.of(),
            "Nablus"
        );

        // Save the original console output.
        originalOut = System.out;

        // Capture printed notifications.
        outputStream = new ByteArrayOutputStream();

        System.setOut(
            new PrintStream(
                outputStream,
                true,
                StandardCharsets.UTF_8
            )
        );
    }

    @AfterEach
    void tearDown() {

        // Restore the original console output.
        System.setOut(originalOut);
    }

    // =====================================================
    // Order Status Notifications
    // =====================================================

    @Test
    void testOrderStatusNotification() {

        observer.onOrderStatusChanged(
            order,
            OrderStatus.OUT_FOR_DELIVERY
        );

        String output = outputStream.toString(
            StandardCharsets.UTF_8
        );

        assertTrue(
            output.contains("ORD-300")
        );

        assertTrue(
            output.contains("OUT_FOR_DELIVERY")
        );

        assertTrue(
            output.contains("Unknown")
        );
    }

    @Test
    void testDeliveredOrderNotification() {

        observer.onOrderStatusChanged(
            order,
            OrderStatus.DELIVERED
        );

        String output = outputStream.toString(
            StandardCharsets.UTF_8
        );

        assertTrue(
            output.contains("DELIVERED")
        );

        assertTrue(
            output.contains("ORD-300")
        );
    }

    @Test
    void testStatusNotificationRejectsNullOrder() {

        assertThrows(
            IllegalArgumentException.class,
            () -> observer.onOrderStatusChanged(
                null,
                OrderStatus.DELIVERED
            )
        );
    }

    @Test
    void testStatusNotificationRejectsNullStatus() {

        assertThrows(
            IllegalArgumentException.class,
            () -> observer.onOrderStatusChanged(
                order,
                null
            )
        );
    }

    // =====================================================
    // Driver Assignment Notifications
    // =====================================================

    @Test
    void testDriverAssignmentNotification() {

        Driver driver = new Driver(
            "D10",
            "Ahmad",
            true,
            2.0,
            10
        );

        observer.onDriverAssigned(
            order,
            driver
        );

        String output = outputStream.toString(
            StandardCharsets.UTF_8
        );

        assertTrue(
            output.contains("Ahmad")
        );

        assertTrue(
            output.contains("ORD-300")
        );

        assertTrue(
            output.contains("Unknown")
        );
    }

    @Test
    void testDriverAssignmentNotificationContainsMessage() {

        Driver driver = new Driver(
            "D11",
            "Omar",
            true,
            3.0,
            15
        );

        observer.onDriverAssigned(
            order,
            driver
        );

        String output = outputStream.toString(
            StandardCharsets.UTF_8
        );

        assertTrue(
            output.contains(
                "Driver Omar has been assigned to order ORD-300"
            )
        );
    }

    @Test
    void testDriverNotificationRejectsNullOrder() {

        Driver driver = new Driver(
            "D12",
            "Ali",
            true,
            4.0,
            20
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> observer.onDriverAssigned(
                null,
                driver
            )
        );
    }

    @Test
    void testDriverNotificationRejectsNullDriver() {

        assertThrows(
            IllegalArgumentException.class,
            () -> observer.onDriverAssigned(
                order,
                null
            )
        );
    }
}