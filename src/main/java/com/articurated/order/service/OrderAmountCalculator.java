package com.articurated.order.service;

import com.articurated.order.domain.OrderItem;
import com.articurated.order.domain.valueobjects.OrderAmount;

import java.util.List;

public interface OrderAmountCalculator {
    OrderAmount calculate(List<OrderItem> items);
}
