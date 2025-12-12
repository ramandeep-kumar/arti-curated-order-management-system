package com.articurated.messaging.consumer;

import com.articurated.payment.gateway.PaymentGatewayClient;
import com.articurated.returns.service.app.ReturnReadService;
import com.articurated.returns.domain.Return;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;

import static org.mockito.Mockito.*;

class RefundMessageConsumerTest {

    private ReturnReadService returnService;
    private PaymentGatewayClient paymentGatewayClient;
    private RefundMessageConsumer consumer;
    private MeterRegistry meterRegistry;
    private Counter attemptsCounter;
    private Counter failuresCounter;

    @BeforeEach
    void setUp() {
        returnService = mock(ReturnReadService.class);
        paymentGatewayClient = mock(PaymentGatewayClient.class);
        meterRegistry = mock(MeterRegistry.class);
        attemptsCounter = mock(Counter.class);
        failuresCounter = mock(Counter.class);

        when(meterRegistry.counter("refund.attempts")).thenReturn(attemptsCounter);
        when(meterRegistry.counter("refund.failures")).thenReturn(failuresCounter);

        consumer = new RefundMessageConsumer(returnService, paymentGatewayClient);
        consumer.setMeterRegistry(meterRegistry);
    }

    @Test
    void handleRefundProcessing_success_incrementsAttempts() throws Exception {
    Return r = new Return();
    r.setId(5L);
    r.setRefundAmount(java.math.BigDecimal.TEN);
        when(returnService.getReturnById(5L)).thenReturn(r);
        when(paymentGatewayClient.processRefund(5L, r.getRefundAmount())).thenReturn(true);

        consumer.handleRefundProcessing(5L);

        verify(meterRegistry, times(1)).counter("refund.attempts");
        verify(attemptsCounter, times(1)).increment();
        verify(meterRegistry, never()).counter("refund.failures");
    }

    @Test
    void handleRefundProcessing_failure_incrementsFailures() throws Exception {
    Return r = new Return();
    r.setId(6L);
    r.setRefundAmount(java.math.BigDecimal.TEN);
        when(returnService.getReturnById(6L)).thenReturn(r);
        when(paymentGatewayClient.processRefund(6L, r.getRefundAmount())).thenReturn(false);

        try {
            consumer.handleRefundProcessing(6L);
        } catch (RuntimeException ignored) {
            // expected
        }

        verify(meterRegistry, times(1)).counter("refund.attempts");
        verify(attemptsCounter, times(1)).increment();
        verify(meterRegistry, times(1)).counter("refund.failures");
        verify(failuresCounter, atLeastOnce()).increment();
    }
}
