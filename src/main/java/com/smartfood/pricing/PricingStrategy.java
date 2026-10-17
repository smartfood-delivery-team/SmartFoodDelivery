package com.smartfood.pricing;

/**
 * Defines a pricing strategy for calculating
 * the final amount of an order.
 */
public interface PricingStrategy {

    /**
     * Calculates the adjusted price.
     *
     * @param basePrice original price
     * @return adjusted price
     */
    double calculatePrice(double basePrice);
}