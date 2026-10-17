package com.smartfood.pricing;

/**
 * Applies a 20% surcharge during peak hours.
 */
public class PeakHourPricingStrategy implements PricingStrategy {

    @Override
    public double calculatePrice(double basePrice) {
        return basePrice * 1.20;
    }
}