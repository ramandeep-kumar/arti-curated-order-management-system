package com.articurated.order.adapter.persistence;

import com.articurated.order.domain.Order;
import com.articurated.order.domain.OrderState;
import com.articurated.order.port.OrderPersistencePort;
import com.articurated.order.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SpringDataOrderPersistenceAdapter implements OrderPersistencePort {

    private final OrderRepository orderRepository;

    public SpringDataOrderPersistenceAdapter(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public Optional<Order> findByIdWithAllDetails(Long id) {
        return orderRepository.findByIdWithAllDetails(id);
    }

    @Override
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public List<Order> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail) {
        return orderRepository.findByCustomerEmailOrderByCreatedAtDesc(customerEmail);
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    @Override
    public Page<Order> findByCurrentState(OrderState state, Pageable pageable) {
        return orderRepository.findByCurrentState(state, pageable);
    }

    @Override
    public List<Order> findByCustomerEmailAndState(String customerEmail, OrderState state) {
        return orderRepository.findByCustomerEmailAndState(customerEmail, state);
    }

    @Override
    public long countByCurrentState(OrderState state) {
        return orderRepository.countByCurrentState(state);
    }
}
