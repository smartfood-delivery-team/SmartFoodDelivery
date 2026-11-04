package com.smartfood.domain;
public class BicycleDriver extends Driver {
    public BicycleDriver(String id,String name,boolean available,double distance,int eta) { super(id,name,available,distance,eta); }
    @Override public DriverType getDriverType() { return DriverType.BICYCLE; }
    @Override public boolean canDeliver(Order order) { return order != null && order.getDeliveryDistanceKm() <= 5.0 && totalItems(order) <= 4; }
    private int totalItems(Order order) { return order.getItems().stream().mapToInt(OrderItem::getQuantity).sum(); }
}
