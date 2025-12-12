package com.articurated.order.port;

import com.articurated.order.domain.OrderStateHistory;

import java.util.List;

public interface OrderStateHistoryPersistencePort {
    OrderStateHistory save(OrderStateHistory history);
    List<OrderStateHistory> findByOrderIdOrderByChangedAtDesc(Long orderId);
}
