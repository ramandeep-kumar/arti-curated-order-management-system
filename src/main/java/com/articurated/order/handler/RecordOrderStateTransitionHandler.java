package com.articurated.order.handler;

import com.articurated.order.domain.Order;
import com.articurated.order.domain.OrderState;
import com.articurated.order.port.OrderStateHistoryPersistencePort;
import com.articurated.statetransition.StateTransitionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RecordOrderStateTransitionHandler implements StateTransitionHandler<Order, OrderState> {

    private final OrderStateHistoryPersistencePort historyPort;

    @Override
    public void handle(Order order, OrderState from, OrderState to, String reason) {
        if (order == null) return;
        com.articurated.order.domain.OrderStateHistory history = com.articurated.order.domain.OrderStateHistory.builder()
            .order(order)
            .fromState(from)
            .toState(to)
            .reason(reason)
            .changedBy("SYSTEM")
            .changedAt(LocalDateTime.now())
            .build();
        historyPort.save(history);
    }
}
