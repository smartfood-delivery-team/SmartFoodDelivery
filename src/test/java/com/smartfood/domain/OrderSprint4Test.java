package com.smartfood.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.smartfood.pricing.ExpressPricingStrategy;
import com.smartfood.pricing.NormalPricingStrategy;
import com.smartfood.pricing.PeakHourPricingStrategy;
import com.smartfood.pricing.PromotionalPricingStrategy;

class OrderSprint4Test {

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order(
            "ORD-S4",
            null,
            null,
            List.of(),
            "Nablus"
        );
    }

    @Test
    void newOrderShouldHaveZeroDeliveryFee() {
        assertEquals(0.0, order.getDeliveryFee());
        assertEquals(0.0, order.getDeliveryDistanceKm());
    }

    @Test
    void zeroDistanceShouldChargeBaseFee() {
        order.calculateDeliveryFee(0.0);

        assertEquals(5.0, order.getDeliveryFee());
    }

    @Test
    void deliveryFeeShouldDependOnDistance() {
        order.calculateDeliveryFee(3.0);

        assertEquals(3.0, order.getDeliveryDistanceKm());
        assertEquals(11.0, order.getDeliveryFee());
    }

    @Test
    void finalTotalShouldIncludeDeliveryFee() {
        order.calculateDeliveryFee(4.0);

        assertEquals(13.0, order.getFinalTotal());
    }

    @Test
    void negativeDistanceShouldBeRejected() {
        assertThrows(
            IllegalArgumentException.class,
            () -> order.calculateDeliveryFee(-1.0)
        );
    }

    @Test
    void nanDistanceShouldBeRejected() {
        assertThrows(
            IllegalArgumentException.class,
            () -> order.calculateDeliveryFee(Double.NaN)
        );
    }

    @Test
    void infiniteDistanceShouldBeRejected() {
        assertThrows(
            IllegalArgumentException.class,
            () -> order.calculateDeliveryFee(Double.POSITIVE_INFINITY)
        );
    }

    @Test
    void defaultDynamicPricingShouldUseNormalTotal() {
        order.calculateDeliveryFee(2.0);

        assertEquals(
            order.getFinalTotal(),
            order.getDynamicFinalTotal()
        );
    }

    @Test
    void normalPricingShouldKeepFinalTotal() {
        order.calculateDeliveryFee(2.0);
        order.setPricingStrategy(new NormalPricingStrategy());

        assertEquals(9.0, order.getDynamicFinalTotal());
    }

    @Test
    void peakHourPricingShouldIncreaseFinalTotal() {
        order.calculateDeliveryFee(2.0);
        order.setPricingStrategy(new PeakHourPricingStrategy());

        assertEquals(10.8, order.getDynamicFinalTotal(), 0.0001);
    }

    @Test
    void expressPricingShouldIncreaseFinalTotal() {
        order.calculateDeliveryFee(2.0);
        order.setPricingStrategy(new ExpressPricingStrategy());

        assertEquals(10.35, order.getDynamicFinalTotal(), 0.0001);
    }

    @Test
    void promotionalPricingShouldReduceFinalTotal() {
        order.calculateDeliveryFee(2.0);
        order.setPricingStrategy(new PromotionalPricingStrategy());

        assertEquals(8.1, order.getDynamicFinalTotal(), 0.0001);
    }

    @Test
    void nullPricingStrategyShouldBeRejected() {
        assertThrows(
            IllegalArgumentException.class,
            () -> order.setPricingStrategy(null)
        );
    }

    @Test
    void selectedPricingStrategyShouldBeReturned() {
        NormalPricingStrategy strategy = new NormalPricingStrategy();

        order.setPricingStrategy(strategy);

        assertSame(strategy, order.getPricingStrategy());
    }

    @Test
    void nullPaymentStatusShouldBeRejected() {
        assertThrows(
            IllegalArgumentException.class,
            () -> order.setPaymentStatus(null)
        );
    }
}