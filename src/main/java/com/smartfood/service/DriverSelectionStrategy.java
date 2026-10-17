package com.smartfood.service;

import com.smartfood.domain.Driver;
import java.util.List;

/**
 * Strategy interface for selecting a delivery driver.
 */
public interface DriverSelectionStrategy {

    /**
     * Selects a driver from the available drivers.
     *
     * @param drivers list of drivers
     * @return selected driver or null if none available
     */
    Driver selectDriver(List<Driver> drivers);
}