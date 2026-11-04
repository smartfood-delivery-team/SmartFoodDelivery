package com.smartfood.domain;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
/** Scheduled delivery; stored dates may be in the past when subsequently read. */
public class ScheduledOrder extends Order {
    private final LocalDateTime scheduledFor;
    public ScheduledOrder(String id,User customer,Restaurant restaurant,List<OrderItem> items,String address,LocalDateTime time) {
        this(id,customer,restaurant,items,address,time,Clock.systemDefaultZone(),false);
    }
    public ScheduledOrder(String id,User customer,Restaurant restaurant,List<OrderItem> items,String address,
                          LocalDateTime time,Clock clock) {
        this(id,customer,restaurant,items,address,time,clock,false);
    }
    private ScheduledOrder(String id,User customer,Restaurant restaurant,List<OrderItem> items,String address,
                           LocalDateTime time,Clock clock,boolean restoring) {
        super(id,customer,restaurant,items,address);
        if (time == null || clock == null) throw new IllegalArgumentException("Schedule and clock required");
        if (!restoring && !time.isAfter(LocalDateTime.now(clock)))
            throw new IllegalArgumentException("Scheduled delivery must be in the future");
        this.scheduledFor=time;
    }
    public static ScheduledOrder restore(String id,User customer,Restaurant restaurant,List<OrderItem> items,
                                         String address,LocalDateTime time) {
        return new ScheduledOrder(id,customer,restaurant,items,address,time,Clock.systemDefaultZone(),true);
    }
    public LocalDateTime getScheduledFor() { return scheduledFor; }
    @Override public OrderType getOrderType() { return OrderType.SCHEDULED; }
}
