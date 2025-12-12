package com.articurated.order.service.domain;

import com.articurated.order.domain.Order;
import com.articurated.order.domain.OrderEvent;
import com.articurated.order.dto.CreateOrderRequest;

public interface DomainOrderService {
    Order createOrderDomain(CreateOrderRequest request);
    Order processStateTransition(Order order, OrderEvent event);
}
