package com.articurated.notification.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SimpleEmailSender implements EmailSender {

    @Override
    public void sendInvoiceEmail(Long orderId, String recipient) {
        log.info("[SimpleEmailSender] Pretending to send invoice {} to {}", orderId, recipient);
        // no-op for now; real integration would call an SMTP/API here
    }
}
