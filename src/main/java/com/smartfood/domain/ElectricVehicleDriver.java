package com.smartfood.domain;
public class ElectricVehicleDriver extends Driver {
    private final double remainingRangeKm;
    public ElectricVehicleDriver(String id,String name,boolean available,double distance,int eta,double remainingRangeKm) {
        super(id,name,available,distance,eta);
        if (!Double.isFinite(remainingRangeKm) || remainingRangeKm < 0)
            throw new IllegalArgumentException("Invalid battery range");
        this.remainingRangeKm=remainingRangeKm;
    }
    @Override public DriverType getDriverType() { return DriverType.ELECTRIC_VEHICLE; }
    @Override public double getRemainingRangeKm() { return remainingRangeKm; }
    @Override public boolean canDeliver(Order order) {
        // Round-trip range with 20% safety margin.
        return order != null && remainingRangeKm >= order.getDeliveryDistanceKm()*2.4;
    }
}
