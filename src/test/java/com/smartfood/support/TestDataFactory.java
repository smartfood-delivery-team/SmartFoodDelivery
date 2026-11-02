package com.smartfood.support;

import com.smartfood.domain.*;

import java.util.List;

/**
 * Creates reusable test objects.
 */
public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static User createCustomer() {
        return new User(
                "lana",
                "test-password-hash",
                Role.CUSTOMER
        );
    }

    public static Restaurant createRestaurant() {
        return new Restaurant(
                "Pizza House",
                true,
                "Nablus",
                0.0
        );
    }

    public static Order createEmptyOrder(String orderId) {
        return new Order(
                orderId,
                createCustomer(),
                createRestaurant(),
                List.of(),
                "Nablus"
        );
    }
}