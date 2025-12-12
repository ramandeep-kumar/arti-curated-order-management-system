package com.articurated.messaging.consumer;

import com.articurated.returns.service.app.ReturnReadService;
import com.articurated.payment.gateway.PaymentGatewayClient;
import com.articurated.shared.config.RabbitMQConfig;
import com.articurated.messaging.util.MessagingMdc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.Message;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefundMessageConsumer {

    private final ReturnReadService returnService;
    private final PaymentGatewayClient paymentGatewayClient;
    private io.micrometer.core.instrument.MeterRegistry meterRegistry;

    // optional injection
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setMeterRegistry(io.micrometer.core.instrument.MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @RabbitListener(queues = RabbitMQConfig.REFUND_QUEUE)
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void handleRefundProcessing(Message<?> message) {
        Object raw = message.getPayload();
        Long returnId = null;
        if (raw instanceof Number) {
            returnId = ((Number) raw).longValue();
        } else if (raw != null) {
            returnId = Long.parseLong(raw.toString());
        }
        org.springframework.amqp.core.Message amqpMessage = (org.springframework.amqp.core.Message) message.getHeaders().get("amqp_receivedMessage");
        String correlationId = null;
        String timestamp = null;
        if (amqpMessage != null) {
            var props = amqpMessage.getMessageProperties();
            Object cid = props.getHeaders() != null ? props.getHeaders().get("correlationId") : null;
            Object ts = props.getHeaders() != null ? props.getHeaders().get("timestamp") : null;
            correlationId = cid != null ? cid.toString() : null;
            timestamp = ts != null ? ts.toString() : null;
        }
        MessagingMdc.putCorrelationId(correlationId);
        MessagingMdc.putMessageTimestamp(timestamp);
        log.info("Processing refund for return: {} (correlationId={}, timestamp={})", returnId, correlationId, timestamp);

        try {
            if (meterRegistry != null) meterRegistry.counter("refund.attempts").increment();
            // Get return details
            var returnEntity = returnService.getReturnById(returnId);

            // Call payment gateway client
            boolean success = paymentGatewayClient.processRefund(returnId, returnEntity.getRefundAmount());
            if (!success) {
                log.warn("Payment gateway failed for returnId={}. Throwing to trigger retry.", returnId);
                throw new RuntimeException("Payment gateway failure");
            }

            log.info("Refund processed for returnId={}", returnId);
        } catch (Exception e) {
            log.error("Failed to process refund for return: {}", returnId, e);
            if (meterRegistry != null) meterRegistry.counter("refund.failures").increment();
            throw new RuntimeException(e);
        } finally {
            MessagingMdc.clear();
        }
    }

    // convenience overload used by tests and internal callers
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void handleRefundProcessing(Long returnId) {
        handleRefundProcessing(new org.springframework.messaging.support.GenericMessage<>(returnId));
    }

    // Payment processing delegated to PaymentGatewayClient
}
