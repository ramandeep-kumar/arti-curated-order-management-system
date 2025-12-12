package com.articurated.order.mapper;

import com.articurated.order.domain.OrderItem;
import com.articurated.order.dto.OrderItemRequest;

import java.util.List;

public interface OrderMapper {
    List<OrderItem> toOrderItems(List<OrderItemRequest> items);
}
