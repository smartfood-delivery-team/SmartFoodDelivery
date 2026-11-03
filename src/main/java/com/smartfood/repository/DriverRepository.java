package com.smartfood.repository;

import com.smartfood.domain.Driver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** In-memory storage for delivery drivers. */
public class DriverRepository implements DriverStore {

    private final Map<String, Driver> drivers = new HashMap<>();

    /** @param driver the driver to save */
    public void save(Driver driver) {
        drivers.put(driver.getId(), driver);
    }

    /**
     * @param id the driver ID
     * @return the driver or null if not found
     */
    public Driver findById(String id) {
        return drivers.get(id);
    }

    /** @return all drivers */
    public List<Driver> findAll() {
        return new ArrayList<>(drivers.values());
    }

    /** @return all available drivers */
    public List<Driver> findAvailableDrivers() {

        List<Driver> availableDrivers = new ArrayList<>();

        for (Driver driver : drivers.values()) {

            if (driver.isAvailable()) {
                availableDrivers.add(driver);
            }
        }

        return availableDrivers;
    }
}