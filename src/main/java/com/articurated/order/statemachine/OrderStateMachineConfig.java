package com.articurated.order.statemachine;

import com.articurated.order.domain.OrderEvent;
import com.articurated.order.domain.OrderState;
import com.articurated.shared.events.GenerateInvoiceEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Configuration
@EnableStateMachine
@RequiredArgsConstructor
public class OrderStateMachineConfig {
    
    private final ApplicationEventPublisher eventPublisher;
    private final com.articurated.invoice.service.app.InvoiceWriteService invoiceWriteService;
    
    @Bean
    public StateMachineBuilder.Builder<OrderState, OrderEvent> stateMachineBuilder() throws Exception {
        StateMachineBuilder.Builder<OrderState, OrderEvent> builder = StateMachineBuilder.builder();
        
        builder.configureStates()
            .withStates()
            .initial(OrderState.PENDING_PAYMENT)
            .states(EnumSet.allOf(OrderState.class));
        
        builder.configureTransitions()
            .withExternal()
                .source(OrderState.PENDING_PAYMENT).target(OrderState.PAID)
                .event(OrderEvent.PAYMENT_RECEIVED)
            .and()
            .withExternal()
                .source(OrderState.PAID).target(OrderState.PROCESSING_IN_WAREHOUSE)
                .event(OrderEvent.START_PROCESSING)
            .and()
            .withExternal()
                .source(OrderState.PROCESSING_IN_WAREHOUSE).target(OrderState.SHIPPED)
                .event(OrderEvent.SHIP_ORDER)
                .action(shipOrderAction())
            .and()
            .withExternal()
                .source(OrderState.SHIPPED).target(OrderState.DELIVERED)
                .event(OrderEvent.DELIVER_ORDER)
            .and()
            .withExternal()
                .source(OrderState.PENDING_PAYMENT).target(OrderState.CANCELLED)
                .event(OrderEvent.CANCEL_ORDER)
            .and()
            .withExternal()
                .source(OrderState.PAID).target(OrderState.CANCELLED)
                .event(OrderEvent.CANCEL_ORDER);
        
        return builder;
    }
    
    @Bean
    public org.springframework.statemachine.StateMachine<OrderState, OrderEvent> stateMachine() throws Exception {
        return stateMachineBuilder().build();
    }
    
    private Action<OrderState, OrderEvent> shipOrderAction() {
        return context -> {
            Long orderId = (Long) context.getExtendedState().getVariables().get("orderId");
            if (orderId != null) {
                // Ensure an Invoice domain entity is created when an order is shipped so
                // downstream consumers (and the invoices API) can find it. Keep publishing
                // the GenerateInvoiceEvent for async PDF/email generation.
                try {
                    invoiceWriteService.generateInvoiceForOrder(orderId);
                } catch (Exception e) {
                    // log and continue to publish event; generating the invoice should not
                    // block the shipping transition in normal operation
                    org.slf4j.LoggerFactory.getLogger(OrderStateMachineConfig.class)
                        .warn("Failed to create invoice entity for order {}: {}", orderId, e.getMessage());
                }
                eventPublisher.publishEvent(new GenerateInvoiceEvent(orderId));
            }
        };
    }
}
