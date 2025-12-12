package com.articurated.order.handler;

import com.articurated.order.domain.Order;
import com.articurated.order.domain.OrderState;
import com.articurated.shared.events.EventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class EmitInvoiceOnPaidHandlerTest {

    @Mock
    private EventPublisher eventPublisher;

    @Test
    void whenTransitionToPaid_doesNotPublishGenerateInvoiceEvent() {
        // Handler intentionally does not publish on payment anymore.
        EmitInvoiceOnPaidHandler handler = new EmitInvoiceOnPaidHandler();
        Order o = new Order();
        o.setId(42L);

        handler.handle(o, OrderState.PENDING_PAYMENT, OrderState.PAID, "paid");

        verify(eventPublisher, times(0)).publishAfterCommit(Mockito.any());
    }

    @Test
    void whenTransitionToOther_doesNotPublish() {
        EmitInvoiceOnPaidHandler handler = new EmitInvoiceOnPaidHandler();
        Order o = new Order();
        o.setId(42L);

        handler.handle(o, OrderState.PENDING_PAYMENT, OrderState.CANCELLED, "cancelled");

        verify(eventPublisher, times(0)).publishAfterCommit(Mockito.any());
    }
}
