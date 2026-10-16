package com.smartfood.service;

import com.smartfood.domain.*;
import com.smartfood.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {
    private OrderRepository repo;
    private OrderService service;
    private User customer;
    private Restaurant restaurant;
    private MenuItem margherita;
    private MenuItem pepperoni;
    private MenuItem water;

    @BeforeEach
    void setUp() {
        repo = new OrderRepository();
        service = new OrderService(repo);
        customer = new User("lana", "1234", Role.CUSTOMER);
        restaurant = new Restaurant("Pizza House", true, "Nablus", 20.0);
        margherita = new MenuItem("Margherita", 25.0, true);
        pepperoni = new MenuItem("Pepperoni", 30.0, false);
        water = new MenuItem("Water", 5.0, true);
        restaurant.addMenuItem(margherita);
        restaurant.addMenuItem(pepperoni);
        restaurant.addMenuItem(water);
    }

    private Order placeValid() {
        return service.placeOrder(customer, restaurant,
                List.of(new OrderItem(margherita, 2)), "Main St 5", "Nablus");
    }

    @Test
    void validOrder_isCreatedPendingWithCorrectTotal() {
        Order order = placeValid();
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(50.0, order.getTotal(), 0.001);
        assertSame(order, repo.findById(order.getId()));
    }

    @Test
    void emptyOrder_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                service.placeOrder(customer, restaurant, Collections.emptyList(), "Main St 5", "Nablus"));
    }

    @Test
    void nullItems_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                service.placeOrder(customer, restaurant, null, "Main St 5", "Nablus"));
    }

    @Test
    void zeroQuantity_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                service.placeOrder(customer, restaurant,
                        List.of(new OrderItem(margherita, 0)), "Main St 5", "Nablus"));
    }

    @Test
    void negativeQuantity_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                service.placeOrder(customer, restaurant,
                        List.of(new OrderItem(margherita, -1)), "Main St 5", "Nablus"));
    }

    @Test
    void unavailableItem_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                service.placeOrder(customer, restaurant,
                        List.of(new OrderItem(pepperoni, 1)), "Main St 5", "Nablus"));
    }

    @Test
    void outsideDeliveryZone_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                service.placeOrder(customer, restaurant,
                        List.of(new OrderItem(margherita, 1)), "Main St 5", "Ramallah"));
    }

    @Test
    void belowMinimumOrder_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                service.placeOrder(customer, restaurant,
                        List.of(new OrderItem(water, 1)), "Main St 5", "Nablus"));
    }

    @Test
    void blankAddress_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                service.placeOrder(customer, restaurant,
                        List.of(new OrderItem(margherita, 1)), "   ", "Nablus"));
    }

    @Test
    void nullAddress_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                service.placeOrder(customer, restaurant,
                        List.of(new OrderItem(margherita, 1)), null, "Nablus"));
    }

    @Test
    void restaurantWithoutZone_acceptsAnyZone() {
        Restaurant open = new Restaurant("Any Place", true);
        open.addMenuItem(margherita);
        Order order = service.placeOrder(customer, open,
                List.of(new OrderItem(margherita, 1)), "Main St 5", "Anywhere");
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void pendingOrder_canBeCancelled() {
        Order order = placeValid();
        Order cancelled = service.cancelOrder(order.getId());
        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    void preparingOrder_cannotBeCancelled() {
        Order order = placeValid();
        order.setStatus(OrderStatus.PREPARING);
        assertThrows(IllegalStateException.class, () -> service.cancelOrder(order.getId()));
    }

    @Test
    void alreadyCancelledOrder_cannotBeCancelledAgain() {
        Order order = placeValid();
        service.cancelOrder(order.getId());
        assertThrows(IllegalStateException.class, () -> service.cancelOrder(order.getId()));
    }

    @Test
    void unknownOrder_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.cancelOrder("ORD-999"));
    }
    @Test
    void nullCustomerZone_whenRestaurantHasZone_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                service.placeOrder(customer, restaurant,
                        List.of(new OrderItem(margherita, 1)), "Main St 5", null));
    }

    @Test
    void confirmedOrder_canBeCancelled() {
        Order order = placeValid();
        order.setStatus(OrderStatus.CONFIRMED);
        Order cancelled = service.cancelOrder(order.getId());
        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
    }
}