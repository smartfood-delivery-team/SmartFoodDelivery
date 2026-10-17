package com.smartfood.pricing;

/**
 * Applies normal pricing without adjustments.
 */
public class NormalPricingStrategy implements PricingStrategy {

    @Override
    public double calculatePrice(double basePrice) {
        return basePrice;
    }
}