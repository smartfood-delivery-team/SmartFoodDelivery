package com.smartfood.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderDomainTest {

    @Test
    void orderExposesItsData() {
        User customer = new User("lana", "1234", Role.CUSTOMER);
        Restaurant r = new Restaurant("Pizza House", true, "Nablus", 20.0);
        OrderItem item = new OrderItem(new MenuItem("Margherita", 25.0, true), 2);
        Order order = new Order("ORD-1", customer, r, List.of(item), "Main St 5");

        assertEquals("ORD-1", order.getId());
        assertSame(customer, order.getCustomer());
        assertSame(r, order.getRestaurant());
        assertEquals(1, order.getItems().size());
        assertEquals("Main St 5", order.getDeliveryAddress());
        assertEquals(50.0, order.getTotal(), 0.001);
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void orderItemExposesItsData() {
        MenuItem m = new MenuItem("Margherita", 25.0, true);
        OrderItem item = new OrderItem(m, 3);
        assertSame(m, item.getMenuItem());
        assertEquals(3, item.getQuantity());
        assertEquals(75.0, item.getSubtotal(), 0.001);
    }

    @Test
    void restaurantExposesZoneAndMinimumOrder() {
        Restaurant r = new Restaurant("Pizza House", true, "Nablus", 20.0);
        assertEquals("Nablus", r.getDeliveryZone());
        assertEquals(20.0, r.getMinimumOrder(), 0.001);
    }
}