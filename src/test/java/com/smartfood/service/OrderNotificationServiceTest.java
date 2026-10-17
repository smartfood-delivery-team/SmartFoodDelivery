package com.smartfood.service;

import com.smartfood.domain.Order;
import com.smartfood.domain.OrderStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    void testObserverReceivesNotification() {

        List<OrderStatus> receivedStatuses =
                new java.util.ArrayList<>();

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
                new java.util.ArrayList<>();

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
                new java.util.ArrayList<>();

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
            () -> notificationService.updateOrderStatus(order, null)
        );
    }
}