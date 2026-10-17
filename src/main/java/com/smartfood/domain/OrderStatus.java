package com.smartfood.domain;

/** Lifecycle states of an order. */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    READY,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}