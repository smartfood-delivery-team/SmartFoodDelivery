package com.smartfood.service;

import com.smartfood.domain.Order;
import com.smartfood.domain.OrderType;
import com.smartfood.domain.StandardOrder;
import com.smartfood.domain.ExpressOrder;
import com.smartfood.domain.ScheduledOrder;
import java.time.LocalDateTime;
import com.smartfood.domain.OrderItem;
import com.smartfood.domain.OrderStatus;
import com.smartfood.domain.Restaurant;
import com.smartfood.domain.User;
import com.smartfood.repository.OrderRepository;

import java.util.List;
import java.util.UUID;

/**
 * Handles placing, cancelling, and updating orders.
 *
 * Validates order details and delivery policies,
 * manages order lifecycle transitions, and
 * notifies observers when order status changes.
 */
public class OrderService {

    private final OrderRepository repository;

    private final OrderNotificationService notificationService;

    /**
     * Creates an OrderService with default notification support.
     */
    public OrderService(OrderRepository repository) {
        this(repository, new OrderNotificationService());
    }

    /**
     * Creates an OrderService with an injected notification service.
     */
    public OrderService(
            OrderRepository repository,
            OrderNotificationService notificationService
    ) {

        if (repository == null || notificationService == null) {
            throw new IllegalArgumentException(
                    "Repository and notification service cannot be null"
            );
        }

        this.repository = repository;
        this.notificationService = notificationService;
    }

    // ==========================================
    // OBSERVER MANAGEMENT
    // ==========================================

    public void addObserver(OrderObserver observer) {
        notificationService.addObserver(observer);
    }

    public void removeObserver(OrderObserver observer) {
        notificationService.removeObserver(observer);
    }

    // ==========================================
    // PLACE ORDER
    // ==========================================

    /**
     * Places a new order after validating its details.
     *
     * @return newly created order with PENDING status
     */
    public Order placeOrder(
            User customer,
            Restaurant restaurant,
            List<OrderItem> items,
            String deliveryAddress,
            String customerZone
    ) {
        return placeOrder(customer,restaurant,items,deliveryAddress,customerZone,OrderType.STANDARD,null);
    }

    public Order placeOrder(User customer,Restaurant restaurant,List<OrderItem> items,
                            String deliveryAddress,String customerZone,OrderType type,LocalDateTime schedule) {
        if (type == null) throw new IllegalArgumentException("Order type required");
        if (type == OrderType.SCHEDULED && (schedule == null || !schedule.isAfter(LocalDateTime.now())))
            throw new IllegalArgumentException("Scheduled time must be in the future");

        // Step 1: Validate customer
        if (customer == null) {
            throw new IllegalArgumentException(
                    "Customer cannot be null"
            );
        }

        // Step 2: Validate restaurant
        if (restaurant == null) {
            throw new IllegalArgumentException(
                    "Restaurant cannot be null"
            );
        }

        if (!restaurant.isOpen()) {
            throw new IllegalArgumentException(
                    "Cannot order from a closed restaurant"
            );
        }

        // Step 3: Validate order items
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException(
                    "Order must contain at least one item"
            );
        }

        for (OrderItem item : items) {

            if (item == null || item.getMenuItem() == null) {
                throw new IllegalArgumentException(
                        "Order contains an invalid item"
                );
            }

            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException(
                        "Invalid quantity"
                );
            }

            if (!item.getMenuItem().isAvailable()) {
                throw new IllegalArgumentException(
                        "Item not available: "
                                + item.getMenuItem().getName()
                );
            }

            double price = item.getMenuItem().getPrice();

            if (!Double.isFinite(price) || price < 0) {
                throw new IllegalArgumentException(
                        "Invalid menu item price"
                );
            }
        }

        // Step 4: Validate delivery address
        if (deliveryAddress == null
                || deliveryAddress.isBlank()) {

            throw new IllegalArgumentException(
                    "Invalid delivery address"
            );
        }

        // Step 5: Validate delivery zone
        String zone = restaurant.getDeliveryZone();

        if (zone != null && !zone.isBlank()) {

            if (customerZone == null
                    || !zone.equalsIgnoreCase(customerZone.trim())) {

                throw new IllegalArgumentException(
                        "Address is outside the delivery zone"
                );
            }
        }

        // Step 6: Calculate the total
        double total = 0.0;

        for (OrderItem item : items) {
            total += item.getSubtotal();
        }

        if (!Double.isFinite(total)) {
            throw new IllegalArgumentException(
                    "Invalid order total"
            );
        }

        // Step 7: Enforce minimum order policy
        if (total < restaurant.getMinimumOrder()) {
            throw new IllegalArgumentException(
                    "Order is below the minimum order value"
            );
        }

        // Step 8: Generate a unique order ID
        String orderId = "ORD-" + UUID.randomUUID();

        // Step 9: Create the order
        Order order = switch (type) {
            case STANDARD -> new StandardOrder(orderId,customer,restaurant,items,deliveryAddress);
            case EXPRESS -> new ExpressOrder(orderId,customer,restaurant,items,deliveryAddress);
            case SCHEDULED -> new ScheduledOrder(orderId,customer,restaurant,items,deliveryAddress,schedule);
        };

        // Step 10: Persist the order
        repository.save(order);

        return order;
    }

    // ==========================================
    // CANCEL ORDER
    // ==========================================

    /**
     * Cancels an order before preparation begins.
     *
     * The existing project policy allows cancellation
     * while the order is PENDING or CONFIRMED.
     */
    public Order cancelOrder(String orderId) {

        Order order = repository.findById(orderId);

        if (order == null) {
            throw new IllegalArgumentException(
                    "Order not found"
            );
        }

        OrderStatus currentStatus = order.getStatus();

        if (currentStatus != OrderStatus.PENDING
                && currentStatus != OrderStatus.CONFIRMED) {

            throw new IllegalStateException(
                    "Order can no longer be cancelled"
            );
        }

        // Update status and notify observers
        notificationService.updateOrderStatus(
                order,
                OrderStatus.CANCELLED
        );

        // Persist the new status
        repository.save(order);

        return order;
    }

    // ==========================================
    // UPDATE ORDER STATUS
    // ==========================================

    /**
     * Updates an order status if the transition is valid.
     */
    public Order updateOrderStatus(
            String orderId,
            OrderStatus newStatus
    ) {

        Order order = repository.findById(orderId);

        if (order == null) {
            throw new IllegalArgumentException(
                    "Order not found"
            );
        }

        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "Order status cannot be null"
            );
        }

        OrderStatus currentStatus = order.getStatus();

        if (!isValidTransition(currentStatus, newStatus)) {

            throw new IllegalStateException(
                    "Invalid order status transition: "
                            + currentStatus + " -> " + newStatus
            );
        }

        // Update status and notify observers
        notificationService.updateOrderStatus(
                order,
                newStatus
        );

        // Persist the updated status
        repository.save(order);

        return order;
    }

    // ==========================================
    // ORDER LIFECYCLE VALIDATION
    // ==========================================

    /**
     * Checks whether an order status transition is valid.
     */
    private boolean isValidTransition(
            OrderStatus current,
            OrderStatus next
    ) {

        if (current == null || next == null) {
            return false;
        }

        switch (current) {

            case PENDING:
                return next == OrderStatus.CONFIRMED;

            case CONFIRMED:
                return next == OrderStatus.PREPARING;

            case PREPARING:
                return next == OrderStatus.READY;

            case READY:
                return next == OrderStatus.OUT_FOR_DELIVERY;

            case OUT_FOR_DELIVERY:
                return next == OrderStatus.DELIVERED;

            case DELIVERED:
            case CANCELLED:
                return false;

            default:
                return false;
        }
    }

    // ==========================================
    // ORDER RETRIEVAL
    // ==========================================

    /**
     * Finds an order by its ID.
     */
    public Order findOrderById(String orderId) {
        return repository.findById(orderId);
    }

    /**
     * Returns all stored orders.
     */
    public List<Order> findAllOrders() {
        return repository.findAll();
    }
}