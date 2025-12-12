package com.articurated.order.service.impl;

import com.articurated.order.domain.OrderItem;
import com.articurated.order.domain.valueobjects.OrderAmount;
import com.articurated.order.service.OrderAmountCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class OrderAmountCalculatorImpl implements OrderAmountCalculator {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.08"); // 8% tax
    private static final BigDecimal SHIPPING_COST = new BigDecimal("15.00");

    @Override
    public OrderAmount calculate(List<OrderItem> items) {
        BigDecimal subtotal = items.stream()
                .map(OrderItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return OrderAmount.calculate(subtotal, TAX_RATE, SHIPPING_COST);
    }
}
