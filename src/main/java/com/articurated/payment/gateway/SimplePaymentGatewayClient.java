package com.articurated.payment.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class SimplePaymentGatewayClient implements PaymentGatewayClient {

    @Override
    public boolean processRefund(Long returnId, BigDecimal amount) throws Exception {
        log.info("[SimplePaymentGatewayClient] Processing refund for {} amount={}", returnId, amount);
        // Keep a short sleep to simulate latency but deterministic success
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        return true;
    }
}
