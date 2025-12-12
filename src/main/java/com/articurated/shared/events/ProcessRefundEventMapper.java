package com.articurated.shared.events;

import com.articurated.messaging.producer.MessageProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProcessRefundEventMapper implements EventMapper {

    private final MessageProducer messageProducer;

    @Override
    public boolean supports(Object event) {
        return event instanceof ProcessRefundEvent;
    }

    @Override
    public void mapAndSend(Object event) {
        ProcessRefundEvent e = (ProcessRefundEvent) event;
        messageProducer.sendRefundProcessingMessage(e.getReturnId());
    }
}
