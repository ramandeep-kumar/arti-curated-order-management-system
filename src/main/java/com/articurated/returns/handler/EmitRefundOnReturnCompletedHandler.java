package com.articurated.returns.handler;

import com.articurated.returns.domain.Return;
import com.articurated.returns.domain.ReturnState;
import com.articurated.shared.events.EventPublisher;
import com.articurated.statetransition.StateTransitionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmitRefundOnReturnCompletedHandler implements StateTransitionHandler<Return, ReturnState> {

    private final EventPublisher eventPublisher;

    @Override
    public void handle(Return returnEntity, ReturnState from, ReturnState to, String reason) {
        if (to == ReturnState.COMPLETED) {
            try {
                eventPublisher.publishAfterCommit(new com.articurated.shared.events.ProcessRefundEvent(returnEntity.getId()));
            } catch (Exception e) {
                // swallow
            }
        }
    }
}
