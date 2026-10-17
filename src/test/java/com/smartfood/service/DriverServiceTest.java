package com.smartfood.service;

import com.smartfood.domain.Driver;
import com.smartfood.domain.Order;
import com.smartfood.domain.OrderStatus;
import com.smartfood.repository.DriverRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests driver management, selection, assignment,
 * and driver assignment notifications.
 */
class DriverServiceTest {

    private DriverRepository repository;

    @BeforeEach
    void setUp() {

        repository = new DriverRepository();

        repository.save(
            new Driver("D1", "Ahmad", true, 5.0, 15)
        );

        repository.save(
            new Driver("D2", "Omar", true, 2.0, 20)
        );

        repository.save(
            new Driver("D3", "Ali", true, 8.0, 10)
        );

        repository.save(
            new Driver("D4", "Khaled", false, 1.0, 5)
        );
    }

    @Test
    void testFindAvailableDrivers() {

        List<Driver> available =
                repository.findAvailableDrivers();

        assertEquals(3, available.size());
    }

    @Test
    void testNearestDriverStrategy() {

        DriverSelectionStrategy strategy =
                new NearestDriverStrategy();

        Driver selected =
                strategy.selectDriver(
                    repository.findAvailableDrivers()
                );

        assertNotNull(selected);
        assertEquals("D2", selected.getId());
    }

    @Test
    void testFastestDriverStrategy() {

        DriverSelectionStrategy strategy =
                new FastestDriverStrategy();

        Driver selected =
                strategy.selectDriver(
                    repository.findAvailableDrivers()
                );

        assertNotNull(selected);
        assertEquals("D3", selected.getId());
    }

    @Test
    void testBusyDriverCannotBeAssigned() {

        Driver driver = repository.findById("D4");

        assertThrows(
            IllegalStateException.class,
            driver::assign
        );
    }

    @Test
    void testDriverBecomesBusyAfterAssignment() {

        Driver driver = repository.findById("D1");

        driver.assign();

        assertFalse(driver.isAvailable());
    }

    @Test
    void testDriverBecomesAvailableAfterRelease() {

        Driver driver = repository.findById("D1");

        driver.assign();
        driver.release();

        assertTrue(driver.isAvailable());
    }

    @Test
    void testNoAvailableDrivers() {

        DriverSelectionStrategy strategy =
                new NearestDriverStrategy();

        Driver selected =
                strategy.selectDriver(List.of());

        assertNull(selected);
    }

    @Test
    void testAssignDriverToOrder() {

        DriverRepository driverRepository =
                new DriverRepository();

        Driver driver =
                new Driver("D1", "Ahmad", true, 2.0, 10);

        driverRepository.save(driver);

        DriverService driverService =
                new DriverService(
                    driverRepository,
                    new NearestDriverStrategy()
                );

        Order order = new Order(
            "ORD-100",
            null,
            null,
            List.of(),
            "Nablus"
        );

        Driver assignedDriver =
                driverService.assignDriver(order);

        assertNotNull(assignedDriver);
        assertEquals("D1", assignedDriver.getId());
        assertSame(driver, order.getAssignedDriver());
        assertFalse(driver.isAvailable());
    }

    @Test
    void testCannotAssignDriverTwice() {

        DriverRepository driverRepository =
                new DriverRepository();

        driverRepository.save(
            new Driver("D1", "Ahmad", true, 2.0, 10)
        );

        driverRepository.save(
            new Driver("D2", "Omar", true, 3.0, 15)
        );

        DriverService driverService =
                new DriverService(
                    driverRepository,
                    new NearestDriverStrategy()
                );

        Order order = new Order(
            "ORD-101",
            null,
            null,
            List.of(),
            "Nablus"
        );

        driverService.assignDriver(order);

        assertThrows(
            IllegalStateException.class,
            () -> driverService.assignDriver(order)
        );
    }

    /**
     * Tests that the observer receives the assigned driver.
     */
    @Test
    void testDriverAssignmentNotifiesObserver() {

        DriverRepository driverRepository =
                new DriverRepository();

        Driver driver =
                new Driver("D1", "Ahmad", true, 2.0, 10);

        driverRepository.save(driver);

        OrderNotificationService notificationService =
                new OrderNotificationService();

        List<Driver> notifiedDrivers = new ArrayList<>();
        List<OrderStatus> receivedStatuses = new ArrayList<>();

        OrderObserver observer = new OrderObserver() {

            @Override
            public void onOrderStatusChanged(
                    Order updatedOrder,
                    OrderStatus newStatus) {

                receivedStatuses.add(newStatus);
            }

            @Override
            public void onDriverAssigned(
                    Order updatedOrder,
                    Driver assignedDriver) {

                notifiedDrivers.add(assignedDriver);
            }
        };

        notificationService.addObserver(observer);

        DriverService driverService =
                new DriverService(
                    driverRepository,
                    new NearestDriverStrategy(),
                    notificationService
                );

        Order order = new Order(
            "ORD-102",
            null,
            null,
            List.of(),
            "Nablus"
        );

        driverService.assignDriver(order);

        // Verify the driver assignment notification.
        assertEquals(1, notifiedDrivers.size());

        assertSame(
            driver,
            notifiedDrivers.get(0)
        );

        assertEquals(
            "Ahmad",
            notifiedDrivers.get(0).getName()
        );

        // Driver assignment must not change the order status.
        assertEquals(OrderStatus.PENDING, order.getStatus());

        // Driver assignment is not a status-change event.
        assertTrue(receivedStatuses.isEmpty());

        // The assigned driver becomes busy.
        assertFalse(driver.isAvailable());
    }
}