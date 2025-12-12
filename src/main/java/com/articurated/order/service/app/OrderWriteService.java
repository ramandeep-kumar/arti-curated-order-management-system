package com.articurated.order.service.app;

import com.articurated.order.domain.Order;
import com.articurated.order.domain.OrderEvent;
import com.articurated.order.dto.CreateOrderRequest;
import com.articurated.order.dto.OrderItemRequest;
import java.util.List;

/**
 * Write-oriented operations for Orders (create/modify). Controllers that trigger changes should depend on this.
 */
public interface OrderWriteService {
    Order createOrder(CreateOrderRequest request);
    Order transitionOrderState(Long orderId, OrderEvent event);
    /** Append items to an existing order and recalculate amounts. Returns the updated order. */
    Order addItems(Long orderId, List<OrderItemRequest> items);
}
