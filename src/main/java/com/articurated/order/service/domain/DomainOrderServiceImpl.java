package com.articurated.order.service.domain;

import com.articurated.order.domain.Order;
import com.articurated.order.domain.OrderItem;
import com.articurated.order.domain.OrderEvent;
import com.articurated.order.dto.CreateOrderRequest;
import com.articurated.order.service.OrderAmountCalculator;
import com.articurated.order.mapper.OrderMapper;
import com.articurated.shared.util.NumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DomainOrderServiceImpl implements DomainOrderService {

    private final OrderAmountCalculator orderAmountCalculator;
    private final NumberGenerator numberGenerator;
    private final OrderMapper orderMapper;

    @Override
    public Order createOrderDomain(CreateOrderRequest request) {
        // Create order items
        List<OrderItem> items = orderMapper.toOrderItems(request.getItems());

        // Calculate amounts
        var amountCalc = orderAmountCalculator.calculate(items);

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
                .currentState(com.articurated.order.domain.OrderState.PENDING_PAYMENT)
                .build();

        items.forEach(item -> {
            item.setOrder(order);
            order.getItems().add(item);
        });

        return order;
    }

    @Override
    public Order processStateTransition(Order order, OrderEvent event) {
        // Pure domain calculation for next state (deterministic)
        switch (order.getCurrentState()) {
            case PENDING_PAYMENT:
                if (event == OrderEvent.PAYMENT_RECEIVED) order.setCurrentState(com.articurated.order.domain.OrderState.PAID);
                if (event == OrderEvent.CANCEL_ORDER) order.setCurrentState(com.articurated.order.domain.OrderState.CANCELLED);
                break;
            case PAID:
                if (event == OrderEvent.START_PROCESSING) order.setCurrentState(com.articurated.order.domain.OrderState.PROCESSING_IN_WAREHOUSE);
                if (event == OrderEvent.CANCEL_ORDER) order.setCurrentState(com.articurated.order.domain.OrderState.CANCELLED);
                break;
            case PROCESSING_IN_WAREHOUSE:
                if (event == OrderEvent.SHIP_ORDER) order.setCurrentState(com.articurated.order.domain.OrderState.SHIPPED);
                break;
            case SHIPPED:
                if (event == OrderEvent.DELIVER_ORDER) order.setCurrentState(com.articurated.order.domain.OrderState.DELIVERED);
                break;
            default:
                // no-op
        }
        return order;
    }
}
