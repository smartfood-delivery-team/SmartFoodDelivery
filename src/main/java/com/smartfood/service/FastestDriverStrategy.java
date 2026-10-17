package com.smartfood.service;

import com.smartfood.domain.Driver;
import java.util.Comparator;
import java.util.List;

/**
 * Selects the fastest available delivery driver.
 */
public class FastestDriverStrategy implements DriverSelectionStrategy {

    @Override
    public Driver selectDriver(List<Driver> drivers) {

        if (drivers == null) {
            throw new IllegalArgumentException(
                "Driver list cannot be null"
            );
        }

        return drivers.stream()
                .filter(Driver::isAvailable)
                .min(Comparator.comparingInt(Driver::getEstimatedTime))
                .orElse(null);
    }
}