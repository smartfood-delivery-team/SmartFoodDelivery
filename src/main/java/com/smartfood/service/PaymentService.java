package com.smartfood.service;

import com.smartfood.domain.Order;
import com.smartfood.domain.PaymentStatus;

/**
 * Handles cash payments for customer orders.
 *
 * Sprint 4 - US4.4: Process Payment.
 */
public class PaymentService {

    /**
     * Processes a cash payment for an order.
     *
     * @param order order being paid for
     * @param amount amount received from the customer
     * @return payment status
     */
    public PaymentStatus processCashPayment(
            Order order, double amount) {

        if (order == null) {
            throw new IllegalArgumentException(
                "Order cannot be null"
            );
        }

        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException(
                "Payment amount must be positive and finite"
            );
        }

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException(
                "Order has already been paid"
            );
        }

        double requiredAmount = order.getDynamicFinalTotal();

        if (Math.abs(amount - requiredAmount) > 0.001) {
            order.setPaymentStatus(PaymentStatus.FAILED);
            return PaymentStatus.FAILED;
        }

        order.setPaymentStatus(PaymentStatus.PAID);

        return PaymentStatus.PAID;
    }
}