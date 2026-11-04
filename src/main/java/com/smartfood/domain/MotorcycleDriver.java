package com.smartfood.domain;
public class MotorcycleDriver extends Driver {
    public MotorcycleDriver(String id,String name,boolean available,double distance,int eta) { super(id,name,available,distance,eta); }
    @Override public DriverType getDriverType() { return DriverType.MOTORCYCLE; }
    @Override public boolean canDeliver(Order order) { return order != null && order.getDeliveryDistanceKm() <= 15.0 && totalItems(order) <= 12; }
    private int totalItems(Order order) { return order.getItems().stream().mapToInt(OrderItem::getQuantity).sum(); }
}
