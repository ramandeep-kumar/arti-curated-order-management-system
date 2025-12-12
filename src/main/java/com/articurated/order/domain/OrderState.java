package com.articurated.order.domain;

public enum OrderState {
    PENDING_PAYMENT,
    PAID,
    PROCESSING_IN_WAREHOUSE,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
