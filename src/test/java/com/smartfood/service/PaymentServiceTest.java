package com.smartfood.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.smartfood.domain.Order;
import com.smartfood.domain.PaymentStatus;

class PaymentServiceTest {

    private PaymentService paymentService;
    private Order order;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService();

        order = new Order(
            "ORD-TEST",
            null,
            null,
            List.of(),
            "Nablus"
        );
    }

    @Test
    void newOrderShouldBeUnpaid() {
        assertEquals(
            PaymentStatus.UNPAID,
            order.getPaymentStatus()
        );
    }

    @Test
    void validCashPaymentShouldBeAccepted() {
        order.calculateDeliveryFee(2.5);

        double amount = order.getDynamicFinalTotal();

        assertEquals(
            PaymentStatus.PAID,
            paymentService.processCashPayment(order, amount)
        );

        assertEquals(
            PaymentStatus.PAID,
            order.getPaymentStatus()
        );
    }

    @Test
    void incorrectCashPaymentShouldFail() {
        order.calculateDeliveryFee(3.0);

        assertEquals(
            PaymentStatus.FAILED,
            paymentService.processCashPayment(order, 5.0)
        );

        assertEquals(
            PaymentStatus.FAILED,
            order.getPaymentStatus()
        );
    }

    @Test
    void nullOrderShouldBeRejected() {
        assertThrows(
            IllegalArgumentException.class,
            () -> paymentService.processCashPayment(null, 100.0)
        );
    }

    @Test
    void zeroPaymentShouldBeRejected() {
        assertThrows(
            IllegalArgumentException.class,
            () -> paymentService.processCashPayment(order, 0.0)
        );
    }

    @Test
    void negativePaymentShouldBeRejected() {
        assertThrows(
            IllegalArgumentException.class,
            () -> paymentService.processCashPayment(order, -10.0)
        );
    }

    @Test
    void nanPaymentShouldBeRejected() {
        assertThrows(
            IllegalArgumentException.class,
            () -> paymentService.processCashPayment(
                order, Double.NaN
            )
        );
    }

    @Test
    void infinitePaymentShouldBeRejected() {
        assertThrows(
            IllegalArgumentException.class,
            () -> paymentService.processCashPayment(
                order, Double.POSITIVE_INFINITY
            )
        );
    }

    @Test
    void duplicatePaymentShouldBeRejected() {
        order.calculateDeliveryFee(1.0);

        double amount = order.getDynamicFinalTotal();

        paymentService.processCashPayment(order, amount);

        assertThrows(
            IllegalStateException.class,
            () -> paymentService.processCashPayment(order, amount)
        );
    }

    @Test
    void failedPaymentCanBeRetried() {
        order.calculateDeliveryFee(2.0);

        assertEquals(
            PaymentStatus.FAILED,
            paymentService.processCashPayment(order, 1.0)
        );

        double correctAmount = order.getDynamicFinalTotal();

        assertEquals(
            PaymentStatus.PAID,
            paymentService.processCashPayment(order, correctAmount)
        );
    }
}