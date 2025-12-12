package com.articurated.shared.events;

import com.articurated.messaging.consumer.InvoiceMessageConsumer;
import com.articurated.messaging.producer.MessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenerateInvoiceEventMapper implements EventMapper {

    private final MessageProducer messageProducer;

    // optional synchronous fallback when Rabbit is unavailable
    @Autowired(required = false)
    private InvoiceMessageConsumer invoiceMessageConsumer;

    @Override
    public boolean supports(Object event) {
        return event instanceof GenerateInvoiceEvent;
    }

    @Override
    public void mapAndSend(Object event) {
        GenerateInvoiceEvent e = (GenerateInvoiceEvent) event;
        try {
            messageProducer.sendInvoiceGenerationMessage(e.getOrderId());
        } catch (Exception ex) {
            log.warn("Failed to send invoice generation message via RabbitMQ for order {} - falling back to synchronous generation", e.getOrderId(), ex);
            if (invoiceMessageConsumer != null) {
                try {
                    invoiceMessageConsumer.handleInvoiceGeneration(e.getOrderId());
                } catch (Exception ex2) {
                    log.error("Synchronous fallback invoice generation failed for order {}", e.getOrderId(), ex2);
                }
            } else {
                log.error("No InvoiceMessageConsumer available for synchronous fallback for order {}", e.getOrderId());
            }
        }
    }
}
