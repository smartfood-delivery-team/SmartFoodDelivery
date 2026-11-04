package com.smartfood.domain;

import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class Sprint5PolymorphismTest {
    private User user() { return new User("lana","hash",Role.CUSTOMER); }
    private Restaurant restaurant() { return new Restaurant("Pizza House",true,"Nablus",0); }
    private List<OrderItem> items(int count) { return List.of(new OrderItem(new MenuItem("Pizza",20,true),count)); }
    @Test void orderTypesAndExpressSupplement() {
        Order standard=new StandardOrder("A",user(),restaurant(),items(1),"Nablus");
        Order express=new ExpressOrder("B",user(),restaurant(),items(1),"Nablus");
        standard.calculateDeliveryFee(4);
        express.calculateDeliveryFee(4);
        assertEquals(OrderType.STANDARD,standard.getOrderType());
        assertEquals(OrderType.EXPRESS,express.getOrderType());
        assertEquals(5,express.getFinalTotal()-standard.getFinalTotal(),0.001);
    }
    @Test void scheduledOrderRejectsPastAndPreservesOldSavedTime() {
        Clock clock=Clock.fixed(Instant.parse("2026-11-04T10:00:00Z"),ZoneOffset.UTC);
        LocalDateTime past=LocalDateTime.of(2026,11,4,9,0);
        LocalDateTime future=LocalDateTime.of(2026,11,4,11,0);
        assertThrows(IllegalArgumentException.class,()->new ScheduledOrder("A",user(),restaurant(),items(1),"Nablus",past,clock));
        assertEquals(future,new ScheduledOrder("B",user(),restaurant(),items(1),"Nablus",future,clock).getScheduledFor());
        assertEquals(past,ScheduledOrder.restore("C",user(),restaurant(),items(1),"Nablus",past).getScheduledFor());
    }
    @Test void polymorphicDriversApplyEligibilityRules() {
        Order order=new StandardOrder("A",user(),restaurant(),items(2),"Nablus");
        order.calculateDeliveryFee(8);
        Driver bicycle=new BicycleDriver("D1","Bike",true,2,5);
        Driver motor=new MotorcycleDriver("D2","Moto",true,3,6);
        Driver car=new CarDriver("D3","Car",true,4,7);
        Driver evLow=new ElectricVehicleDriver("D4","EV",true,4,7,10);
        Driver evEnough=new ElectricVehicleDriver("D5","EV2",true,4,7,40);
        assertFalse(bicycle.canDeliver(order));
        assertTrue(motor.canDeliver(order));
        assertTrue(car.canDeliver(order));
        assertFalse(evLow.canDeliver(order));
        assertTrue(evEnough.canDeliver(order));
    }
    @Test void carCapacityAndMotorcycleDistanceAreEnforced() {
        Order large=new StandardOrder("A",user(),restaurant(),items(41),"Nablus");
        large.calculateDeliveryFee(2);
        assertFalse(new CarDriver("C","Car",true,1,2).canDeliver(large));
        Order distant=new StandardOrder("B",user(),restaurant(),items(1),"Nablus");
        distant.calculateDeliveryFee(20);
        assertFalse(new MotorcycleDriver("M","Moto",true,1,2).canDeliver(distant));
    }
}
