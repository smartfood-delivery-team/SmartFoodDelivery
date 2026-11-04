package com.smartfood.domain;
public class CarDriver extends Driver {
    public CarDriver(String id,String name,boolean available,double distance,int eta) { super(id,name,available,distance,eta); }
    @Override public DriverType getDriverType() { return DriverType.CAR; }
    @Override public boolean canDeliver(Order order) { return order != null && order.getDeliveryDistanceKm() <= 100.0 && totalItems(order) <= 40; }
    private int totalItems(Order order) { return order.getItems().stream().mapToInt(OrderItem::getQuantity).sum(); }
}
