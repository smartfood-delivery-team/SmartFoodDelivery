package com.smartfood.repository;

import com.smartfood.domain.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderRepositoryTest {

    @Test
    void findAllReturnsSavedOrders() {
        OrderRepository repo = new InMemoryOrderRepository();
        Order order = new Order("ORD-1", new User("lana", "1234", Role.CUSTOMER),
                new Restaurant("Pizza House", true), List.of(), "Main St 5");
        repo.save(order);
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void findByIdUnknownReturnsNull() {
        assertNull(new InMemoryOrderRepository().findById("ORD-999"));
    }
}