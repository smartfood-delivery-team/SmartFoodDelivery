package com.smartfood.pricing;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests all dynamic pricing strategies.
 */
class PricingStrategyTest {

    private static final double DELTA = 0.0001;

    @Test
    void normalPricingShouldKeepOriginalPrice() {
        PricingStrategy strategy = new NormalPricingStrategy();

        assertEquals(100.0, strategy.calculatePrice(100.0), DELTA);
    }

    @Test
    void peakHourPricingShouldAddTwentyPercent() {
        PricingStrategy strategy = new PeakHourPricingStrategy();

        assertEquals(120.0, strategy.calculatePrice(100.0), DELTA);
    }

    @Test
    void expressPricingShouldAddFifteenPercent() {
        PricingStrategy strategy = new ExpressPricingStrategy();

        assertEquals(115.0, strategy.calculatePrice(100.0), DELTA);
    }

    @Test
    void promotionalPricingShouldApplyTenPercentDiscount() {
        PricingStrategy strategy = new PromotionalPricingStrategy();

        assertEquals(90.0, strategy.calculatePrice(100.0), DELTA);
    }

    @Test
    void allStrategiesShouldHandleZeroPrice() {
        PricingStrategy[] strategies = {
            new NormalPricingStrategy(),
            new PeakHourPricingStrategy(),
            new ExpressPricingStrategy(),
            new PromotionalPricingStrategy()
        };

        for (PricingStrategy strategy : strategies) {
            assertEquals(0.0, strategy.calculatePrice(0.0), DELTA);
        }
    }
}