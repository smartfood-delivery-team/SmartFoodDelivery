package com.smartfood.service;

import com.smartfood.domain.Driver;
import com.smartfood.domain.Order;
import com.smartfood.domain.OrderStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderNotificationServiceTest {

    private OrderNotificationService notificationService;
    private Order order;

    @BeforeEach
    void setUp() {

        notificationService = new OrderNotificationService();

        order = new Order(
            "ORD-200",
            null,
            null,
            List.of(),
            "Nablus"
        );
    }

    @Test
    void testOrderStatusChanges() {

        notificationService.updateOrderStatus(
            order,
            OrderStatus.CONFIRMED
        );

        assertEquals(
            OrderStatus.CONFIRMED,
            order.getStatus()
        );
    }

    @Test
    void testObserverReceivesNotification() {

        List<OrderStatus> receivedStatuses =
                new ArrayList<>();

        notificationService.addObserver(
            (updatedOrder, newStatus) ->
                receivedStatuses.add(newStatus)
        );

        notificationService.updateOrderStatus(
            order,
            OrderStatus.OUT_FOR_DELIVERY
        );

        assertEquals(1, receivedStatuses.size());

        assertEquals(
            OrderStatus.OUT_FOR_DELIVERY,
            receivedStatuses.get(0)
        );
    }

    @Test
    void testDeliveredNotification() {

        List<OrderStatus> receivedStatuses =
                new ArrayList<>();

        notificationService.addObserver(
            (updatedOrder, newStatus) ->
                receivedStatuses.add(newStatus)
        );

        notificationService.updateOrderStatus(
            order,
            OrderStatus.DELIVERED
        );

        assertTrue(
            receivedStatuses.contains(OrderStatus.DELIVERED)
        );
    }

    @Test
    void testRemovedObserverDoesNotReceiveNotification() {

        List<OrderStatus> receivedStatuses =
                new ArrayList<>();

        OrderObserver observer =
            (updatedOrder, newStatus) ->
                receivedStatuses.add(newStatus);

        notificationService.addObserver(observer);

        notificationService.removeObserver(observer);

        notificationService.updateOrderStatus(
            order,
            OrderStatus.CONFIRMED
        );

        assertTrue(receivedStatuses.isEmpty());
    }

    @Test
    void testNullStatusRejected() {

        assertThrows(
            IllegalArgumentException.class,
            () -> notificationService.updateOrderStatus(
                order,
                null
            )
        );
    }

    // ==========================================
    // Mockito Tests
    // ==========================================

    /**
     * Verifies that a mocked observer receives
     * the correct order status notification.
     */
    @Test
    void testStatusNotificationUsingMockito() {

        OrderObserver mockObserver =
                mock(OrderObserver.class);

        notificationService.addObserver(mockObserver);

        notificationService.updateOrderStatus(
            order,
            OrderStatus.OUT_FOR_DELIVERY
        );

        verify(mockObserver, times(1))
            .onOrderStatusChanged(
                order,
                OrderStatus.OUT_FOR_DELIVERY
            );
    }

    /**
     * Verifies that a mocked observer receives
     * a notification when an order is delivered.
     */
    @Test
    void testDeliveredNotificationUsingMockito() {

        OrderObserver mockObserver =
                mock(OrderObserver.class);

        notificationService.addObserver(mockObserver);

        notificationService.updateOrderStatus(
            order,
            OrderStatus.DELIVERED
        );

        verify(mockObserver, times(1))
            .onOrderStatusChanged(
                order,
                OrderStatus.DELIVERED
            );
    }

    /**
     * Verifies that the mocked observer receives
     * the assigned driver information.
     */
    @Test
    void testDriverAssignmentNotificationUsingMockito() {

        OrderObserver mockObserver =
                mock(OrderObserver.class);

        notificationService.addObserver(mockObserver);

        Driver driver = new Driver(
            "D1",
            "Ahmad",
            true,
            2.0,
            10
        );

        notificationService.notifyDriverAssigned(
            order,
            driver
        );

        verify(mockObserver, times(1))
            .onDriverAssigned(
                order,
                driver
            );
    }

    /**
     * Verifies that a removed mock observer
     * does not receive notifications.
     */
    @Test
    void testRemovedMockObserverReceivesNoNotification() {

        OrderObserver mockObserver =
                mock(OrderObserver.class);

        notificationService.addObserver(mockObserver);

        notificationService.removeObserver(mockObserver);

        notificationService.updateOrderStatus(
            order,
            OrderStatus.CONFIRMED
        );

        verifyNoInteractions(mockObserver);
    }
}