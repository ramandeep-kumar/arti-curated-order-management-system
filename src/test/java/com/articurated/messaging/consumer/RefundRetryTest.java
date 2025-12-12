package com.articurated.messaging.consumer;

import com.articurated.payment.gateway.PaymentGatewayClient;
import com.articurated.returns.domain.Return;
import com.articurated.returns.service.app.ReturnReadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@SpringBootTest(classes = RefundRetryTest.TestConfig.class)
@ActiveProfiles("test")
class RefundRetryTest {

    @Autowired
    private RefundMessageConsumer consumer;

    @Autowired
    private ReturnReadService returnReadService;

    @Autowired
    private PaymentGatewayClient paymentGatewayClient;

    @Test
    void paymentGatewayFailsOnceThenSucceeds_triggersRetry() throws Exception {
        Return r = new Return();
        r.setId(77L);
        r.setRefundAmount(new BigDecimal("12.00"));

        when(returnReadService.getReturnById(77L)).thenReturn(r);

        // First call throws, second returns true (deterministic)
        java.util.concurrent.atomic.AtomicInteger calls = new java.util.concurrent.atomic.AtomicInteger(0);
        when(paymentGatewayClient.processRefund(eq(77L), any())).thenAnswer(invocation -> {
            int c = calls.incrementAndGet();
            if (c == 1) throw new RuntimeException("simulated transient failure");
            return true;
        });

        // Call consumer; first attempt will fail and be retried by @Retryable
        consumer.handleRefundProcessing(77L);

        // Expect that processRefund was called twice (one failed, one succeeded)
        verify(paymentGatewayClient, times(2)).processRefund(eq(77L), any());
    }

    @org.springframework.context.annotation.Configuration
    @org.springframework.retry.annotation.EnableRetry
    static class TestConfig {
        @org.springframework.context.annotation.Bean
        public ReturnReadService returnReadService() {
            return org.mockito.Mockito.mock(ReturnReadService.class);
        }

        @org.springframework.context.annotation.Bean
        public com.articurated.payment.gateway.PaymentGatewayClient paymentGatewayClient() {
            return org.mockito.Mockito.mock(com.articurated.payment.gateway.PaymentGatewayClient.class);
        }

        @org.springframework.context.annotation.Bean
        public RefundMessageConsumer refundMessageConsumer(ReturnReadService r, com.articurated.payment.gateway.PaymentGatewayClient p) {
            return new RefundMessageConsumer(r, p);
        }
    }
}
