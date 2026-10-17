package com.smartfood.service;

import com.smartfood.domain.Driver;
import com.smartfood.domain.Order;
import com.smartfood.domain.OrderStatus;
import com.smartfood.repository.DriverRepository;

import java.util.List;

/**
 * Handles driver selection, assignment, and release.
 */
public class DriverService {

    private final DriverRepository driverRepository;
    private DriverSelectionStrategy selectionStrategy;
    private final OrderNotificationService notificationService;

    /**
     * Creates a driver service.
     *
     * @param driverRepository repository containing drivers
     * @param selectionStrategy strategy used to select drivers
     */
    public DriverService(DriverRepository driverRepository,
                         DriverSelectionStrategy selectionStrategy) {

        this(
            driverRepository,
            selectionStrategy,
            new OrderNotificationService()
        );
    }

    /**
     * Creates a driver service with notification support.
     *
     * @param driverRepository repository containing drivers
     * @param selectionStrategy driver selection strategy
     * @param notificationService notification service
     */
    public DriverService(DriverRepository driverRepository,
                         DriverSelectionStrategy selectionStrategy,
                         OrderNotificationService notificationService) {

        if (driverRepository == null
                || selectionStrategy == null
                || notificationService == null) {

            throw new IllegalArgumentException(
                "Repository, strategy and notification service cannot be null"
            );
        }

        this.driverRepository = driverRepository;
        this.selectionStrategy = selectionStrategy;
        this.notificationService = notificationService;
    }

    /**
     * Changes the driver selection strategy.
     *
     * @param selectionStrategy new selection strategy
     */
    public void setSelectionStrategy(
            DriverSelectionStrategy selectionStrategy) {

        if (selectionStrategy == null) {
            throw new IllegalArgumentException(
                "Strategy cannot be null"
            );
        }

        this.selectionStrategy = selectionStrategy;
    }

    /**
     * Registers an observer for notifications.
     *
     * @param observer observer to register
     */
    public void addObserver(OrderObserver observer) {
        notificationService.addObserver(observer);
    }

    /**
     * Selects an available driver and assigns them to an order.
     *
     * @param order order requiring delivery
     * @return assigned driver
     */
    public Driver assignDriver(Order order) {

        if (order == null) {
            throw new IllegalArgumentException(
                "Order cannot be null"
            );
        }

        if (order.getAssignedDriver() != null) {
            throw new IllegalStateException(
                "Order already has an assigned driver"
            );
        }

        List<Driver> availableDrivers =
                driverRepository.findAvailableDrivers();

        Driver selectedDriver =
                selectionStrategy.selectDriver(availableDrivers);

        if (selectedDriver == null
                || !selectedDriver.isAvailable()) {

            throw new IllegalStateException(
                "No available drivers found"
            );
        }

        // Assign the selected driver to the order.
        // The driver becomes busy automatically.
        order.assignDriver(selectedDriver);

        // Notify observers when a driver is assigned.
        notificationService.notifyDriverAssigned(
            order,
            selectedDriver
        );

        return selectedDriver;
    }

    /**
     * Releases the assigned driver after delivery or cancellation.
     *
     * @param order completed or cancelled order
     */
    public void releaseDriver(Order order) {

        if (order == null) {
            throw new IllegalArgumentException(
                "Order cannot be null"
            );
        }

        Driver driver = order.getAssignedDriver();

        if (driver == null) {
            throw new IllegalStateException(
                "Order has no assigned driver"
            );
        }

        if (order.getStatus() != OrderStatus.DELIVERED
                && order.getStatus() != OrderStatus.CANCELLED) {

            throw new IllegalStateException(
                "Order must be delivered or cancelled before releasing driver"
            );
        }

        driver.release();
    }
}