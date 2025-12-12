package com.articurated.order.mapper;

import com.articurated.order.domain.Order;
import com.articurated.order.dto.OrderResponse;

public interface OrderResponseMapper {
    OrderResponse toResponse(Order order);
}
