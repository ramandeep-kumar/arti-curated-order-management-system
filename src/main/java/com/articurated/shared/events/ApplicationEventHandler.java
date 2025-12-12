package com.articurated.shared.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationEventHandler {

    private final List<EventMapper> mappers;

    @EventListener
    @Async
    public void handleGenerateInvoiceEvent(GenerateInvoiceEvent event) {
        dispatch(event, "generate invoice for order " + event.getOrderId());
    }

    @EventListener
    @Async
    public void handleProcessRefundEvent(ProcessRefundEvent event) {
        dispatch(event, "process refund for return " + event.getReturnId());
    }

    private void dispatch(Object event, String description) {
        log.info("Handling event: {}", description);
        try {
            for (EventMapper mapper : mappers) {
                if (mapper.supports(event)) {
                    mapper.mapAndSend(event);
                    return;
                }
            }
            log.warn("No EventMapper found for event: {}", event.getClass().getSimpleName());
        } catch (Exception e) {
            log.error("Failed to dispatch event: {}", description, e);
        }
    }
}
