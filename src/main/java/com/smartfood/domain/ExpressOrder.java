package com.smartfood.domain;
import java.util.List;
/** Sprint 5 ExpressOrder subtype. */
public class ExpressOrder extends Order {
    public ExpressOrder(String id, User customer, Restaurant restaurant, List<OrderItem> items, String deliveryAddress) {
        super(id,customer,restaurant,items,deliveryAddress);
    }
    @Override public OrderType getOrderType() { return OrderType.EXPRESS; }
    
    @Override public double getFinalTotal() { return super.getFinalTotal() + 5.0; }

}
