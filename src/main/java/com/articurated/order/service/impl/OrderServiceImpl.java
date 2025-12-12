package com.articurated.order.service.impl;

import com.articurated.order.domain.*;
import com.articurated.order.dto.CreateOrderRequest;
import com.articurated.order.dto.OrderItemRequest;
import com.articurated.order.port.OrderPersistencePort;
import com.articurated.order.port.OrderStateHistoryPersistencePort;
import com.articurated.order.service.OrderService;
import com.articurated.order.service.OrderAmountCalculator;
import com.articurated.order.mapper.OrderMapper;
import com.articurated.order.statemachine.OrderStateMachineManager;
import com.articurated.order.domain.valueobjects.OrderAmount;
import com.articurated.shared.exception.OrderNotFoundException;
import com.articurated.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.statemachine.support.DefaultStateMachineContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
 
import com.articurated.shared.util.NumberGenerator;
import java.util.stream.Collectors;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

@Deprecated
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    
    private final OrderPersistencePort orderRepository;
    private final OrderStateHistoryPersistencePort stateHistoryRepository;
    private final OrderStateMachineManager stateMachineManager;
    private final OrderAmountCalculator orderAmountCalculator;
    private final NumberGenerator numberGenerator;
    private final OrderMapper orderMapper;
    
    @Override
    public Order createOrder(CreateOrderRequest request) {
        log.info("Creating order for customer: {}", request.getCustomerEmail());
        
    // Create order items
    List<OrderItem> items = orderMapper.toOrderItems(request.getItems());
        
    // Calculate amounts
    OrderAmount amountCalc = orderAmountCalculator.calculate(items);
        
        // Create order with flattened structure
        Order order = Order.builder()
            .orderNumber(numberGenerator.generate("ORD-"))
            .customerEmail(request.getCustomerEmail())
            .customerFirstName(request.getFirstName())
            .customerLastName(request.getLastName())
            .street(request.getAddress().getStreet())
            .city(request.getAddress().getCity())
            .state(request.getAddress().getState())
            .zipCode(request.getAddress().getZipCode())
            .country(request.getAddress().getCountry())
            .subtotal(amountCalc.getSubtotal())
            .tax(amountCalc.getTax())
            .shipping(amountCalc.getShipping())
            .total(amountCalc.getTotal())
            .currentState(OrderState.PENDING_PAYMENT)
            .build();
            
        // Set order reference in items and add them to order's items collection
        items.forEach(item -> {
            item.setOrder(order);
            order.getItems().add(item);
        });
        
        // Save order with items (cascade will save items automatically)
        Order savedOrder = orderRepository.save(order);
        
        recordStateChange(savedOrder, null, OrderState.PENDING_PAYMENT, "Order created");
        
        log.info("Order created successfully with ID: {}", savedOrder.getId());
        return savedOrder;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));
    }
    
    @Override
    public Order transitionOrderState(Long orderId, OrderEvent event) {
        Order order = getOrderById(orderId);
        OrderState previousState = order.getCurrentState();
        
        log.info("Transitioning order {} from {} with event {}", orderId, previousState, event);
        
    // Ensure state machine is reset to the order's current state and carry the orderId in extended state
    stateMachineManager.prepareStateMachineForOrder(order.getCurrentState(), orderId);

    // Build a message to prefer synchronous handling in the state machine implementation
    Message<OrderEvent> msg = MessageBuilder.withPayload(event)
        .setHeader("orderId", orderId)
        .build();

    // Determine next state deterministically and persist it; send event to state machine for side-effects
    OrderState deterministicNext = getNextState(previousState, event);

    if (deterministicNext.equals(previousState)) {
        // No valid state change for this event. Treat as idempotent/no-op and return current order.
        log.info("No state change for order {}: currentState={} event={}. Returning current order.", orderId, previousState, event);
        return order;
    }

    // Persist the deterministic state change
    order.setCurrentState(deterministicNext);
    order = orderRepository.save(order);
    recordStateChange(order, previousState, deterministicNext, "State transition via " + event);
    log.info("Order {} transitioned from {} to {} via event {}", orderId, previousState, deterministicNext, event);

    // Fire the event asynchronously/synchronously for side-effects; we don't rely on it for persistence
    try {
        stateMachineManager.sendEvent(msg);
    } catch (Exception e) {
        try {
            stateMachineManager.sendEvent(event);
        } catch (Exception ex) {
            log.warn("Failed to send event to state machine for order {}", orderId, ex);
        }
    }
        
        return order;
    }

    // Simple state transition logic (deterministic fallback)
    private OrderState getNextState(OrderState currentState, OrderEvent event) {
        switch (currentState) {
            case PENDING_PAYMENT:
                if (event == OrderEvent.PAYMENT_RECEIVED) return OrderState.PAID;
                if (event == OrderEvent.CANCEL_ORDER) return OrderState.CANCELLED;
                break;
            case PAID:
                if (event == OrderEvent.START_PROCESSING) return OrderState.PROCESSING_IN_WAREHOUSE;
                if (event == OrderEvent.CANCEL_ORDER) return OrderState.CANCELLED;
                break;
            case PROCESSING_IN_WAREHOUSE:
                if (event == OrderEvent.SHIP_ORDER) return OrderState.SHIPPED;
                break;
            case SHIPPED:
                if (event == OrderEvent.DELIVER_ORDER) return OrderState.DELIVERED;
                break;
            case DELIVERED:
                // No transitions from delivered
                break;
            case CANCELLED:
                // No transitions from cancelled
                break;
        }
        return currentState; // No transition
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByCustomerEmail(String customerEmail) {
        return orderRepository.findByCustomerEmailOrderByCreatedAtDesc(customerEmail);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<OrderStateHistory> getOrderHistory(Long orderId) {
        return stateHistoryRepository.findByOrderIdOrderByChangedAtDesc(orderId);
    }
    
    // item mapping moved to OrderMapper
    
    // calculation moved to OrderAmountCalculator

    private void recordStateChange(Order order, OrderState fromState, OrderState toState, String reason) {
        OrderStateHistory history = OrderStateHistory.builder()
            .order(order)
            .fromState(fromState)
            .toState(toState)
            .reason(reason)
            .changedBy("SYSTEM")
            .changedAt(LocalDateTime.now())
            .build();
            
        stateHistoryRepository.save(history);
    }
    
    // Order number generation moved to shared NumberGenerator
    
    // State machine handling moved to OrderStateMachineManager
    
    
    
}
