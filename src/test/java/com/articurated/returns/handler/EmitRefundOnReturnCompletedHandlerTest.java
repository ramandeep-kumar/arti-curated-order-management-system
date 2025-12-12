package com.articurated.returns.handler;

import com.articurated.returns.domain.Return;
import com.articurated.returns.domain.ReturnState;
import com.articurated.shared.events.EventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.Mockito;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class EmitRefundOnReturnCompletedHandlerTest {

    @Mock
    private EventPublisher eventPublisher;

    @Test
    void whenTransitionToCompleted_publishesProcessRefundEvent() {
        EmitRefundOnReturnCompletedHandler handler = new EmitRefundOnReturnCompletedHandler(eventPublisher);
        Return r = new Return();
        r.setId(7L);

        handler.handle(r, ReturnState.RECEIVED, ReturnState.COMPLETED, "complete");

    verify(eventPublisher, times(1)).publishAfterCommit(Mockito.any());
    }

    @Test
    void whenTransitionToOther_doesNotPublish() {
        EmitRefundOnReturnCompletedHandler handler = new EmitRefundOnReturnCompletedHandler(eventPublisher);
        Return r = new Return();
        r.setId(7L);

        handler.handle(r, ReturnState.APPROVED, ReturnState.IN_TRANSIT, "in transit");

    verify(eventPublisher, times(0)).publishAfterCommit(Mockito.any());
    }
}
