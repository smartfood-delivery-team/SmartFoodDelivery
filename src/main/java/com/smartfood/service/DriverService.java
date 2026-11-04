package com.smartfood.service;

import com.smartfood.domain.Driver;
import com.smartfood.domain.Order;
import com.smartfood.domain.OrderStatus;
import com.smartfood.repository.DriverStore;

import java.util.List;

/**
 * Manages delivery driver selection, assignment, and release.
 *
 * Uses the Strategy design pattern to select drivers
 * and the Observer design pattern to send notifications.
 *
 * Driver availability is persisted through DriverStore.
 */
public class DriverService {

    private final DriverStore driverRepository;

    private DriverSelectionStrategy selectionStrategy;

    private final OrderNotificationService notificationService;

    // =====================================================
    // CONSTRUCTOR 1 - WITHOUT NOTIFICATION SERVICE
    // =====================================================

    public DriverService(
            DriverStore driverRepository,
            DriverSelectionStrategy selectionStrategy
    ) {
        this(
                driverRepository,
                selectionStrategy,
                new OrderNotificationService()
        );
    }

    // =====================================================
    // CONSTRUCTOR 2 - WITH NOTIFICATION SERVICE
    // =====================================================

    public DriverService(
            DriverStore driverRepository,
            DriverSelectionStrategy selectionStrategy,
            OrderNotificationService notificationService
    ) {

        if (driverRepository == null) {
            throw new IllegalArgumentException(
                    "Driver repository cannot be null"
            );
        }

        if (selectionStrategy == null) {
            throw new IllegalArgumentException(
                    "Selection strategy cannot be null"
            );
        }

        if (notificationService == null) {
            throw new IllegalArgumentException(
                    "Notification service cannot be null"
            );
        }

        this.driverRepository = driverRepository;
        this.selectionStrategy = selectionStrategy;
        this.notificationService = notificationService;
    }

    // =====================================================
    // CHANGE DRIVER SELECTION STRATEGY
    // =====================================================

    public void setSelectionStrategy(
            DriverSelectionStrategy selectionStrategy
    ) {

        if (selectionStrategy == null) {
            throw new IllegalArgumentException(
                    "Strategy cannot be null"
            );
        }

        this.selectionStrategy = selectionStrategy;
    }

    // =====================================================
    // REGISTER OBSERVER
    // =====================================================

    public void addObserver(OrderObserver observer) {

        notificationService.addObserver(observer);
    }

    // =====================================================
    // ASSIGN DRIVER TO ORDER
    // =====================================================

    public Driver assignDriver(Order order) {

        // Step 1: Validate order
        if (order == null) {
            throw new IllegalArgumentException(
                    "Order cannot be null"
            );
        }

        // Step 2: Prevent duplicate assignment
        if (order.getAssignedDriver() != null) {
            throw new IllegalStateException(
                    "Order already has an assigned driver"
            );
        }

        // Step 3: Check order status
        if (order.getStatus() == OrderStatus.CANCELLED
                || order.getStatus() == OrderStatus.DELIVERED) {

            throw new IllegalStateException(
                    "Cannot assign a driver to a cancelled or delivered order"
            );
        }

        // Step 4: Retrieve available drivers
        List<Driver> availableDrivers =
                driverRepository.findAvailableDrivers();

        // Sprint 5: select only drivers whose type-specific rules allow this order.
        List<Driver> eligibleDrivers = availableDrivers.stream()
                .filter(driver -> driver.canDeliver(order))
                .toList();
        Driver selectedDriver = selectionStrategy.selectDriver(eligibleDrivers);

        // Step 6: Validate selected driver
        if (selectedDriver == null
                || !selectedDriver.isAvailable()) {

            throw new IllegalStateException(
                    "No available drivers found"
            );
        }

        // Prevent a strategy from returning an unrelated driver
        boolean driverInAvailableList = eligibleDrivers.stream()
                .anyMatch(driver ->
                        driver.getId().equals(selectedDriver.getId())
                );

        if (!driverInAvailableList) {
            throw new IllegalStateException(
                    "Selected driver is not in the available drivers list"
            );
        }

        // Step 7: Assign driver to order
        // This automatically marks the driver as busy
        order.assignDriver(selectedDriver);

        // Step 8: Persist driver's availability
        driverRepository.save(selectedDriver);

        // Step 9: Notify registered observers
        notificationService.notifyDriverAssigned(
                order,
                selectedDriver
        );

        return selectedDriver;
    }

    // =====================================================
    // RELEASE DRIVER
    // =====================================================

    public void releaseDriver(Order order) {

        // Step 1: Validate order
        if (order == null) {
            throw new IllegalArgumentException(
                    "Order cannot be null"
            );
        }

        // Step 2: Retrieve assigned driver
        Driver driver = order.getAssignedDriver();

        if (driver == null) {
            throw new IllegalStateException(
                    "Order has no assigned driver"
            );
        }

        // Step 3: Validate order status
        if (order.getStatus() != OrderStatus.DELIVERED
                && order.getStatus() != OrderStatus.CANCELLED) {

            throw new IllegalStateException(
                    "Order must be delivered or cancelled before releasing driver"
            );
        }

        // Step 4: Release driver
        driver.release();

        // Step 5: Save updated availability
        driverRepository.save(driver);
    }
    /**
     * Returns all currently available delivery drivers.
     *
     * @return list of available drivers
     */
    public List<Driver> getAvailableDrivers() {

        return driverRepository.findAvailableDrivers();
    }
}