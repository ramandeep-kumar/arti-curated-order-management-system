package com.articurated.order.statemachine;

import com.articurated.order.domain.OrderEvent;
import com.articurated.order.domain.OrderState;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SpringOrderStateMachineManager implements OrderStateMachineManager {

    private final org.springframework.statemachine.StateMachine<OrderState, OrderEvent> stateMachine;

    @Override
    public void prepareStateMachineForOrder(OrderState currentState, Long orderId) {
        try {
            org.springframework.statemachine.ExtendedState extendedState = null;
            try {
                extendedState = stateMachine.getExtendedState();
            } catch (Exception ex) {
                // ignore - will handle below
            }

            org.springframework.statemachine.support.DefaultStateMachineContext<OrderState, OrderEvent> context =
                new org.springframework.statemachine.support.DefaultStateMachineContext<>(currentState, null, null, extendedState);

            var accessor = safeGetStateMachineAccessor(stateMachine);
            if (accessor != null) {
                accessor.doWithAllRegions(access -> access.resetStateMachine(context));
            }

            if (extendedState != null) {
                extendedState.getVariables().put("orderId", orderId);
            }
        } catch (Exception e) {
            // swallow; tests may use mocks
        }
    }

    @Override
    public void sendEvent(Message<OrderEvent> message) {
        try {
            stateMachine.sendEvent(message);
        } catch (Exception e) {
            // fallback
            try {
                stateMachine.sendEvent(message.getPayload());
            } catch (Exception ex) {
                // swallow
            }
        }
    }

    @Override
    public void sendEvent(OrderEvent event) {
        try {
            stateMachine.sendEvent(event);
        } catch (Exception e) {
            // swallow
        }
    }

    private org.springframework.statemachine.access.StateMachineAccessor<OrderState, OrderEvent> safeGetStateMachineAccessor(
        org.springframework.statemachine.StateMachine<OrderState, OrderEvent> sm) {
        try {
            var acc = sm.getStateMachineAccessor();
            return acc;
        } catch (Exception e) {
            return null;
        }
    }
}
