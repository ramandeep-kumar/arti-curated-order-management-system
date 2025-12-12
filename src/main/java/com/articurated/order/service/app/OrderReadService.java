package com.articurated.order.service.app;

import com.articurated.order.domain.Order;
import com.articurated.order.domain.OrderStateHistory;

import java.util.List;

/**
 * Read-only operations for Orders used by controllers and read-only clients.
 *
 * Purpose: keep controller dependencies narrow (ISP) so controllers only depend on read operations.
 */
public interface OrderReadService {
    /** Retrieve an order by id. */
    Order getOrderById(Long orderId);

    /** Find orders for a customer ordered by creation date desc. */
    List<Order> getOrdersByCustomerEmail(String customerEmail);

    /** Retrieve ordered history entries for an order. */
    List<OrderStateHistory> getOrderHistory(Long orderId);
}
