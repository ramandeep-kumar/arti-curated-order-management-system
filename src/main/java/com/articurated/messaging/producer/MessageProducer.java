package com.articurated.messaging.producer;

import com.articurated.shared.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageProducer {
    
    private final RabbitTemplate rabbitTemplate;
    
    public void sendInvoiceGenerationMessage(Long orderId) {
        log.info("Sending invoice generation message for order: {}", orderId);
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "invoice.generate", orderId, message -> {
                // set a correlation id and timestamp for tracing
                String cid = UUID.randomUUID().toString();
                // set correlation id header and (where supported) the properties correlation id
                try {
                    message.getMessageProperties().setCorrelationId(cid);
                } catch (NoSuchMethodError | Exception ex) {
                    // fallback to header only if the properties API differs
                    message.getMessageProperties().setHeader("correlationId", cid);
                }
                message.getMessageProperties().setHeader("timestamp", System.currentTimeMillis());
                message.getMessageProperties().setHeader("correlationId", cid);
                return message;
            });
            log.info("Invoice generation message sent successfully for order: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to send invoice generation message for order: {}", orderId, e);
            throw e;
        }
    }
    
    public void sendRefundProcessingMessage(Long returnId) {
        log.info("Sending refund processing message for return: {}", returnId);
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "refund.process", returnId, message -> {
                // set a correlation id and timestamp for tracing
                String cid = UUID.randomUUID().toString();
                try {
                    message.getMessageProperties().setCorrelationId(cid);
                } catch (NoSuchMethodError | Exception ex) {
                    message.getMessageProperties().setHeader("correlationId", cid);
                }
                message.getMessageProperties().setHeader("timestamp", System.currentTimeMillis());
                message.getMessageProperties().setHeader("correlationId", cid);
                return message;
            });
            log.info("Refund processing message sent successfully for return: {}", returnId);
        } catch (Exception e) {
            log.error("Failed to send refund processing message for return: {}", returnId, e);
            throw e;
        }
    }
}
