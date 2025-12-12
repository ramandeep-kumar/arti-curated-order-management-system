package com.articurated.order.handler;

import com.articurated.order.domain.Order;
import com.articurated.order.domain.OrderState;
import com.articurated.statetransition.StateTransitionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmitInvoiceOnPaidHandler implements StateTransitionHandler<Order, OrderState> {
    @Override
    public void handle(Order order, OrderState from, OrderState to, String reason) {
        // Intentionally do NOT auto-generate PDF on payment.
        // PDF generation should be explicitly triggered via the Invoice creation API.
        // This handler will no longer publish GenerateInvoiceEvent when order transitions to PAID.
    }
}

