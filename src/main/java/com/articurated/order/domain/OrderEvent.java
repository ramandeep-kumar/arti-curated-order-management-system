package com.articurated.order.domain;

public enum OrderEvent {
    PAYMENT_RECEIVED,
    START_PROCESSING,
    SHIP_ORDER,
    DELIVER_ORDER,
    CANCEL_ORDER
}
