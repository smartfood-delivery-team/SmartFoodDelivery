package com.smartfood.service;

import com.smartfood.domain.Driver;
import com.smartfood.domain.Order;
import com.smartfood.domain.OrderStatus;
import com.smartfood.repository.DriverRepository;

import java.util.List;

/**
 * Manages delivery driver selection, assignment, and release.
 *
 * <p>This service uses the Strategy design pattern to select
 * the most suitable available driver for an order.
 * It also supports the Observer design pattern by notifying
 * registered observers when a driver is assigned.</p>
 *
 * <p>A driver becomes unavailable after assignment and can
 * be released when the order is delivered or cancelled.</p>
 */
public class DriverService {

    /**
     * Repository responsible for storing and retrieving
     * delivery driver information.
     */
    private final DriverRepository driverRepository;

    /**
     * Strategy used to select the most suitable
     * available driver for an order.
     */
    private DriverSelectionStrategy selectionStrategy;

    /**
     * Notification service responsible for informing
     * registered observers about driver assignments.
     */
    private final OrderNotificationService notificationService;

    /**
     * Constructs a DriverService using the provided repository
     * and driver selection strategy.
     *
     * <p>A new OrderNotificationService is created automatically.</p>
     *
     * @param driverRepository repository containing delivery drivers
     * @param selectionStrategy strategy used to select drivers
     * @throws IllegalArgumentException if the repository
     *         or selection strategy is null
     */
    public DriverService(
            DriverRepository driverRepository,
            DriverSelectionStrategy selectionStrategy) {

        this(
            driverRepository,
            selectionStrategy,
            new OrderNotificationService()
        );
    }

    /**
     * Constructs a DriverService with notification support.
     *
     * @param driverRepository repository containing delivery drivers
     * @param selectionStrategy strategy used to select drivers
     * @param notificationService service used to send notifications
     * @throws IllegalArgumentException if any parameter is null
     */
    public DriverService(
            DriverRepository driverRepository,
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
     * Changes the strategy used to select delivery drivers.
     *
     * <p>This method allows switching between different
     * selection strategies, such as selecting the nearest
     * or fastest available driver.</p>
     *
     * @param selectionStrategy new driver selection strategy
     * @throws IllegalArgumentException if the strategy is null
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
     * Registers an observer to receive order notifications.
     *
     * @param observer observer to register
     */
    public void addObserver(OrderObserver observer) {

        notificationService.addObserver(observer);
    }

    /**
     * Selects an available driver and assigns the driver
     * to the specified order.
     *
     * <p>The selection is performed using the currently
     * configured driver selection strategy.</p>
     *
     * <p>After assignment, the selected driver becomes busy
     * and registered observers are notified.</p>
     *
     * @param order order requiring delivery
     * @return the driver assigned to the order
     * @throws IllegalArgumentException if the order is null
     * @throws IllegalStateException if the order already has
     *         an assigned driver or no available driver is found
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
     * Releases the driver assigned to an order.
     *
     * <p>A driver can only be released when the order
     * status is DELIVERED or CANCELLED.</p>
     *
     * <p>After release, the driver becomes available
     * for future delivery assignments.</p>
     *
     * @param order completed or cancelled order
     * @throws IllegalArgumentException if the order is null
     * @throws IllegalStateException if the order has no assigned
     *         driver or its status is neither DELIVERED nor CANCELLED
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