package com.smartfood.service;

import com.smartfood.domain.Driver;
import com.smartfood.domain.Order;
import com.smartfood.domain.OrderStatus;
import com.smartfood.repository.DriverRepository;

import com.smartfood.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests driver management, selection, assignment,
 * release, and driver assignment notifications.
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

    // =====================================================
    // Existing Tests
    // =====================================================

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

        Order order = TestDataFactory.createEmptyOrder("ORD-1");

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

        Order order = TestDataFactory.createEmptyOrder("ORD-1");

        driverService.assignDriver(order);

        assertThrows(
            IllegalStateException.class,
            () -> driverService.assignDriver(order)
        );
    }

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

        Order order = TestDataFactory.createEmptyOrder("ORD-1");


        driverService.assignDriver(order);

        assertEquals(1, notifiedDrivers.size());

        assertSame(
            driver,
            notifiedDrivers.get(0)
        );

        assertEquals(
            "Ahmad",
            notifiedDrivers.get(0).getName()
        );

        assertEquals(
            OrderStatus.PENDING,
            order.getStatus()
        );

        assertTrue(receivedStatuses.isEmpty());

        assertFalse(driver.isAvailable());
    }

    @Test
    void testAssignDriverRejectsNullOrder() {

        DriverService driverService =
                new DriverService(
                    repository,
                    new NearestDriverStrategy()
                );

        assertThrows(
            IllegalArgumentException.class,
            () -> driverService.assignDriver(null)
        );
    }

    // =====================================================
    // New Tests - Release Driver
    // =====================================================

    @Test
    void testReleaseDriverAfterDelivery() {

        DriverService service = new DriverService(
            repository,
            new NearestDriverStrategy()
        );

        Order order = TestDataFactory.createEmptyOrder("ORD-1");

        Driver driver = service.assignDriver(order);

        assertFalse(driver.isAvailable());

        order.setStatus(OrderStatus.DELIVERED);

        service.releaseDriver(order);

        assertTrue(driver.isAvailable());
    }

    @Test
    void testReleaseDriverAfterCancellation() {

        DriverService service = new DriverService(
            repository,
            new NearestDriverStrategy()
        );

        Order order = TestDataFactory.createEmptyOrder("ORD-1");

        Driver driver = service.assignDriver(order);

        assertFalse(driver.isAvailable());

        order.setStatus(OrderStatus.CANCELLED);

        service.releaseDriver(order);

        assertTrue(driver.isAvailable());
    }

    @Test
    void testCannotReleaseDriverBeforeCompletion() {

        DriverService service = new DriverService(
            repository,
            new NearestDriverStrategy()
        );

        Order order = TestDataFactory.createEmptyOrder("ORD-1");

        Driver driver = service.assignDriver(order);

        assertThrows(
            IllegalStateException.class,
            () -> service.releaseDriver(order)
        );

        assertFalse(driver.isAvailable());
    }

    @Test
    void testCannotReleaseOrderWithoutDriver() {

        DriverService service = new DriverService(
            repository,
            new NearestDriverStrategy()
        );

        Order order = TestDataFactory.createEmptyOrder("ORD-1");

        order.setStatus(OrderStatus.DELIVERED);

        assertThrows(
            IllegalStateException.class,
            () -> service.releaseDriver(order)
        );
    }

    @Test
    void testReleaseDriverRejectsNullOrder() {

        DriverService service = new DriverService(
            repository,
            new NearestDriverStrategy()
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> service.releaseDriver(null)
        );
    }

    // =====================================================
    // New Tests - Driver Selection Strategy
    // =====================================================

    @Test
    void testChangeSelectionStrategy() {

        DriverService service = new DriverService(
            repository,
            new NearestDriverStrategy()
        );

        service.setSelectionStrategy(
            new FastestDriverStrategy()
        );

        Order order = TestDataFactory.createEmptyOrder("ORD-1");


        Driver selected = service.assignDriver(order);

        assertEquals("D3", selected.getId());
    }

    @Test
    void testRejectNullSelectionStrategy() {

        DriverService service = new DriverService(
            repository,
            new NearestDriverStrategy()
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> service.setSelectionStrategy(null)
        );
    }

    @Test
    void testAssignDriverWhenNoDriversAvailable() {

        DriverRepository emptyRepository =
                new DriverRepository();

        DriverService service = new DriverService(
            emptyRepository,
            new NearestDriverStrategy()
        );

        Order order = TestDataFactory.createEmptyOrder("ORD-1");
        assertThrows(
            IllegalStateException.class,
            () -> service.assignDriver(order)
        );

        assertNull(order.getAssignedDriver());
    }

    // =====================================================
    // New Tests - Constructor Validation
    // =====================================================

    @Test
    void testConstructorRejectsNullRepository() {

        assertThrows(
            IllegalArgumentException.class,
            () -> new DriverService(
                null,
                new NearestDriverStrategy(),
                new OrderNotificationService()
            )
        );
    }

    @Test
    void testConstructorRejectsNullStrategy() {

        assertThrows(
            IllegalArgumentException.class,
            () -> new DriverService(
                repository,
                null,
                new OrderNotificationService()
            )
        );
    }

    @Test
    void testConstructorRejectsNullNotificationService() {

        assertThrows(
            IllegalArgumentException.class,
            () -> new DriverService(
                repository,
                new NearestDriverStrategy(),
                null
            )
        );
    }

    // =====================================================
    // New Tests - Observer Notifications
    // =====================================================

    @Test
    void testAddObserverThroughDriverService() {

        DriverService service = new DriverService(
            repository,
            new NearestDriverStrategy()
        );

        List<Driver> notifications = new ArrayList<>();

        OrderObserver observer = new OrderObserver() {

            @Override
            public void onOrderStatusChanged(
                    Order order,
                    OrderStatus status) {
                // Not needed for this test.
            }

            @Override
            public void onDriverAssigned(
                    Order order,
                    Driver driver) {

                notifications.add(driver);
            }
        };

        service.addObserver(observer);

        Order order = TestDataFactory.createEmptyOrder("ORD-1");




        Driver assigned = service.assignDriver(order);

        assertEquals(1, notifications.size());

        assertSame(
            assigned,
            notifications.get(0)
        );
    }
}