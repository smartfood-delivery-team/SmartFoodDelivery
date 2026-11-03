package com.smartfood.repository;

import com.smartfood.domain.Driver;
import java.util.List;

public interface DriverStore {

    void save(Driver driver);

    Driver findById(String id);

    List<Driver> findAll();

    List<Driver> findAvailableDrivers();
}