package com.smartfood.pricing;

/**
 * Applies a 10% promotional discount.
 */
public class PromotionalPricingStrategy implements PricingStrategy {

    @Override
    public double calculatePrice(double basePrice) {
        return basePrice * 0.90;
    }
}