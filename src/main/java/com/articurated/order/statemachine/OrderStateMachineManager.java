package com.articurated.order.statemachine;

import com.articurated.order.domain.OrderEvent;
import com.articurated.order.domain.OrderState;
import org.springframework.messaging.Message;

public interface OrderStateMachineManager {
    void prepareStateMachineForOrder(OrderState currentState, Long orderId);
    void sendEvent(Message<OrderEvent> message);
    void sendEvent(OrderEvent event);
}
