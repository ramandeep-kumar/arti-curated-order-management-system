package com.articurated.payment.gateway;

import java.math.BigDecimal;

public interface PaymentGatewayClient {
    /**
     * Process a refund for the provided returnId and amount. Returns true on success.
     */
    boolean processRefund(Long returnId, BigDecimal amount) throws Exception;
}
