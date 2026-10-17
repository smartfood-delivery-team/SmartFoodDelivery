package com.smartfood.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.smartfood.domain.Order;
import com.smartfood.domain.OrderStatus;
import com.smartfood.repository.OrderRepository;

import static org.mockito.Mockito.*;

class OrderStatusTransitionTest {

    private OrderService orderService;
    private Order order;
    private OrderRepository repository;

    @BeforeEach
    void setUp() {

        repository = mock(OrderRepository.class);

        order = new Order(
            "ORD-S4",
            null,
            null,
            List.of(),
            "Nablus"
        );

        when(repository.findById("ORD-S4")).thenReturn(order);

        orderService = new OrderService(
            repository,
            new OrderNotificationService()
        );
    }

    @Test
    void pendingToConfirmedShouldSucceed() {

        orderService.updateOrderStatus(
            "ORD-S4", OrderStatus.CONFIRMED
        );

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    void confirmedToPreparingShouldSucceed() {

        order.setStatus(OrderStatus.CONFIRMED);

        orderService.updateOrderStatus(
            "ORD-S4", OrderStatus.PREPARING
        );

        assertEquals(OrderStatus.PREPARING, order.getStatus());
    }

    @Test
    void preparingToReadyShouldSucceed() {

        order.setStatus(OrderStatus.PREPARING);

        orderService.updateOrderStatus(
            "ORD-S4", OrderStatus.READY
        );

        assertEquals(OrderStatus.READY, order.getStatus());
    }

    @Test
    void readyToOutForDeliveryShouldSucceed() {

        order.setStatus(OrderStatus.READY);

        orderService.updateOrderStatus(
            "ORD-S4", OrderStatus.OUT_FOR_DELIVERY
        );

        assertEquals(OrderStatus.OUT_FOR_DELIVERY, order.getStatus());
    }

    @Test
    void outForDeliveryToDeliveredShouldSucceed() {

        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);

        orderService.updateOrderStatus(
            "ORD-S4", OrderStatus.DELIVERED
        );

        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    void skippingOrderStagesShouldBeRejected() {

        assertThrows(
            IllegalStateException.class,
            () -> orderService.updateOrderStatus(
                "ORD-S4", OrderStatus.DELIVERED
            )
        );

        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void deliveredOrderCannotBeModified() {

        order.setStatus(OrderStatus.DELIVERED);

        assertThrows(
            IllegalStateException.class,
            () -> orderService.updateOrderStatus(
                "ORD-S4", OrderStatus.PREPARING
            )
        );
    }

    @Test
    void cancelledOrderCannotBeModified() {

        order.setStatus(OrderStatus.CANCELLED);

        assertThrows(
            IllegalStateException.class,
            () -> orderService.updateOrderStatus(
                "ORD-S4", OrderStatus.CONFIRMED
            )
        );
    }

    @Test
    void sameStatusTransitionShouldBeRejected() {

        assertThrows(
            IllegalStateException.class,
            () -> orderService.updateOrderStatus(
                "ORD-S4", OrderStatus.PENDING
            )
        );
    }

    @Test
    void nullStatusShouldBeRejected() {

        assertThrows(
            IllegalArgumentException.class,
            () -> orderService.updateOrderStatus(
                "ORD-S4", null
            )
        );
    }

    @Test
    void missingOrderShouldBeRejected() {

        assertThrows(
            IllegalArgumentException.class,
            () -> orderService.updateOrderStatus(
                "UNKNOWN", OrderStatus.CONFIRMED
            )
        );
    }

    @Test
    void pendingOrderCanBeCancelled() {

        orderService.cancelOrder("ORD-S4");

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void confirmedOrderCanBeCancelled() {

        order.setStatus(OrderStatus.CONFIRMED);

        orderService.cancelOrder("ORD-S4");

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void preparingOrderCannotBeCancelled() {

        order.setStatus(OrderStatus.PREPARING);

        assertThrows(
            IllegalStateException.class,
            () -> orderService.cancelOrder("ORD-S4")
        );
    }

    @Test
    void cancelledOrderCannotBeCancelledAgain() {

        order.setStatus(OrderStatus.CANCELLED);

        assertThrows(
            IllegalStateException.class,
            () -> orderService.cancelOrder("ORD-S4")
        );
    }
}