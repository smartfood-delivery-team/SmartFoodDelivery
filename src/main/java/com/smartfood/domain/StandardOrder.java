package com.smartfood.domain;
import java.util.List;
/** Sprint 5 StandardOrder subtype. */
public class StandardOrder extends Order {
    public StandardOrder(String id, User customer, Restaurant restaurant, List<OrderItem> items, String deliveryAddress) {
        super(id,customer,restaurant,items,deliveryAddress);
    }
    @Override public OrderType getOrderType() { return OrderType.STANDARD; }
    
}
