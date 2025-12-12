package com.articurated.order.port;

import com.articurated.order.domain.Order;
import com.articurated.order.domain.OrderState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface OrderPersistencePort {
    Optional<Order> findById(Long id);
    Optional<Order> findByIdWithAllDetails(Long id);
    Order save(Order order);
    List<Order> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail);
    Optional<Order> findByOrderNumber(String orderNumber);
    Page<Order> findByCurrentState(OrderState state, Pageable pageable);
    List<Order> findByCustomerEmailAndState(String customerEmail, OrderState state);
    long countByCurrentState(OrderState state);
}
