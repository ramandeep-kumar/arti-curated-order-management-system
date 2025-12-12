package com.articurated.order.mapper;

import com.articurated.order.domain.OrderItem;
import com.articurated.order.dto.OrderItemRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapperImpl implements OrderMapper {

    @Override
    public List<OrderItem> toOrderItems(List<OrderItemRequest> items) {
        if (items == null) return java.util.Collections.emptyList();
        return items.stream()
            .map(this::toOrderItem)
            .collect(Collectors.toList());
    }

    private OrderItem toOrderItem(OrderItemRequest request) {
        BigDecimal total = request.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
        return OrderItem.builder()
            .productName(request.getProductName())
            .price(request.getPrice())
            .quantity(request.getQuantity())
            .total(total)
            .build();
    }
}
