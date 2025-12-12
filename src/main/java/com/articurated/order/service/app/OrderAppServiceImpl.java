package com.articurated.order.service.app;

import com.articurated.order.domain.Order;
import com.articurated.order.domain.OrderEvent;
import com.articurated.order.domain.OrderState;
import com.articurated.order.domain.OrderStateHistory;
import com.articurated.order.dto.CreateOrderRequest;
import com.articurated.order.port.OrderPersistencePort;
import com.articurated.order.port.OrderStateHistoryPersistencePort;
import com.articurated.statetransition.StateTransitionHandlerRegistry;
import com.articurated.order.service.OrderService;
import com.articurated.order.service.domain.DomainOrderService;
import com.articurated.order.statemachine.OrderStateMachineManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderAppServiceImpl implements OrderAppService, OrderService, com.articurated.order.service.app.OrderReadService, com.articurated.order.service.app.OrderWriteService {

    private final OrderPersistencePort orderRepository;
    private final OrderStateHistoryPersistencePort stateHistoryRepository;
    private final DomainOrderService domainOrderService;
    private final OrderStateMachineManager stateMachineManager;
    private final StateTransitionHandlerRegistry handlerRegistry;
    private final com.articurated.order.mapper.OrderMapper orderMapper;
    private final com.articurated.order.service.OrderAmountCalculator orderAmountCalculator;

    @Override
    public Order createOrder(CreateOrderRequest request) {
        Order order = domainOrderService.createOrderDomain(request);
        Order saved = orderRepository.save(order);
        recordStateChange(saved, null, OrderState.PENDING_PAYMENT, "Order created");
        log.info("Order created successfully with ID: {}", saved.getId());
        return saved;
    }

    @Override
    public Order addItems(Long orderId, java.util.List<com.articurated.order.dto.OrderItemRequest> newItems) {
        // Load order with items if available
    Order order = orderRepository.findByIdWithAllDetails(orderId).orElseThrow(() -> new com.articurated.shared.exception.OrderNotFoundException("Order not found"));

        // Only allow adding items when order is in allowed states
        switch (order.getCurrentState()) {
            case PENDING_PAYMENT:
            case PAID:
                break;
            default:
                throw new com.articurated.shared.exception.BusinessException("Cannot add items to order in state: " + order.getCurrentState());
        }

        // Map DTOs to domain items
        java.util.List<com.articurated.order.domain.OrderItem> items = orderMapper.toOrderItems(newItems);

        // Attach to order and set reference
        items.forEach(item -> {
            item.setOrder(order);
            order.getItems().add(item);
        });

        // Recalculate amounts for full item list
        var amount = orderAmountCalculator.calculate(order.getItems());
        order.setSubtotal(amount.getSubtotal());
        order.setTax(amount.getTax());
        order.setShipping(amount.getShipping());
        order.setTotal(amount.getTotal());

        // Persist and return updated order
        Order saved = orderRepository.save(order);
        recordStateChange(saved, saved.getCurrentState(), saved.getCurrentState(), "Items appended");
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId) {
    return orderRepository.findById(orderId).orElseThrow(() -> new com.articurated.shared.exception.OrderNotFoundException("Order not found"));
    }

    @Override
    public Order transitionOrderState(Long orderId, OrderEvent event) {
        Order order = getOrderById(orderId);
        OrderState previousState = order.getCurrentState();

        // prepare state machine
        stateMachineManager.prepareStateMachineForOrder(order.getCurrentState(), orderId);

        // Domain-level deterministic transition
        Order updated = domainOrderService.processStateTransition(order, event);

        if (updated.getCurrentState().equals(previousState)) return order;

        // Persist and record
        order = orderRepository.save(updated);
        recordStateChange(order, previousState, order.getCurrentState(), "State transition via " + event);

        // Send event for side-effects
        Message<OrderEvent> msg = MessageBuilder.withPayload(event).setHeader("orderId", orderId).build();
        try {
            stateMachineManager.sendEvent(msg);
        } catch (Exception e) {
            log.warn("Failed to send state machine event for order {}", orderId, e);
        }

        return order;
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

    private void recordStateChange(Order order, OrderState fromState, OrderState toState, String reason) {
        // Delegate to registered handlers so side-effects are pluggable
        for (var h : handlerRegistry.getHandlers()) {
            try {
                @SuppressWarnings("unchecked")
                com.articurated.statetransition.StateTransitionHandler<Order, OrderState> handler = (com.articurated.statetransition.StateTransitionHandler<Order, OrderState>) h;
                handler.handle(order, fromState, toState, reason);
            } catch (ClassCastException ex) {
                // handler not applicable for this entity type - ignore
            } catch (Exception e) {
                // swallow to avoid breaking main flow
            }
        }
    }
}
