package com.articurated.order.service;

import com.articurated.order.domain.Order;
import com.articurated.order.domain.OrderEvent;
import com.articurated.order.domain.OrderStateHistory;
import com.articurated.order.dto.CreateOrderRequest;

import java.util.List;

public interface OrderService {
    Order createOrder(CreateOrderRequest request);
    Order getOrderById(Long orderId);
    Order transitionOrderState(Long orderId, OrderEvent event);
    List<Order> getOrdersByCustomerEmail(String customerEmail);
    List<OrderStateHistory> getOrderHistory(Long orderId);
}
