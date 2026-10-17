package com.smartfood.pricing;

/**
 * Applies a 15% surcharge for express delivery.
 */
public class ExpressPricingStrategy implements PricingStrategy {

    @Override
    public double calculatePrice(double basePrice) {
        return basePrice * 1.15;
    }
}