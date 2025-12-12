package com.articurated.messaging.util;

import org.slf4j.MDC;

public final class MessagingMdc {
    public static final String CORRELATION_ID = "correlationId";
    public static final String MESSAGE_TIMESTAMP = "messageTimestamp";

    private MessagingMdc() {}

    public static void putCorrelationId(String correlationId) {
        if (correlationId != null) MDC.put(CORRELATION_ID, correlationId);
    }

    public static void putMessageTimestamp(String timestamp) {
        if (timestamp != null) MDC.put(MESSAGE_TIMESTAMP, timestamp);
    }

    public static void clear() {
        MDC.remove(CORRELATION_ID);
        MDC.remove(MESSAGE_TIMESTAMP);
    }
}
