package com.articurated.notification.email;

public interface EmailSender {
    /**
     * Send an invoice email to the provided recipient for the given orderId.
     */
    void sendInvoiceEmail(Long orderId, String recipient);
}
