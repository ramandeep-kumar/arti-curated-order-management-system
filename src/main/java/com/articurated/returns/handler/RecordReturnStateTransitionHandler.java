package com.articurated.returns.handler;

import com.articurated.returns.domain.Return;
import com.articurated.returns.domain.ReturnState;
import com.articurated.returns.port.ReturnStateHistoryPersistencePort;
import com.articurated.statetransition.StateTransitionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RecordReturnStateTransitionHandler implements StateTransitionHandler<Return, ReturnState> {

    private final ReturnStateHistoryPersistencePort historyPort;

    @Override
    public void handle(Return returnEntity, ReturnState from, ReturnState to, String reason) {
        if (returnEntity == null) return;
        com.articurated.returns.domain.ReturnStateHistory history = com.articurated.returns.domain.ReturnStateHistory.builder()
            .returnEntity(returnEntity)
            .fromState(from)
            .toState(to)
            .reason(reason)
            .changedBy("SYSTEM")
            .changedAt(LocalDateTime.now())
            .build();
        historyPort.save(history);
    }
}
