package com.articurated.invoice.service.domain;

import com.articurated.invoice.domain.Invoice;
import com.articurated.invoice.domain.InvoiceStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DomainInvoiceServiceImpl implements DomainInvoiceService {

    @Override
    public Invoice prepareInvoiceForOrder(Long orderId, com.articurated.order.domain.Order order) {
        // Default to ISSUED; if order indicates payment already happened mark PAID
        InvoiceStatus initialStatus = InvoiceStatus.ISSUED;
        LocalDateTime paidAt = null;
        if (order != null && (order.getCurrentState() == com.articurated.order.domain.OrderState.PAID
                || order.getCurrentState() == com.articurated.order.domain.OrderState.SHIPPED
                || order.getCurrentState() == com.articurated.order.domain.OrderState.DELIVERED)) {
            initialStatus = InvoiceStatus.PAID;
            paidAt = LocalDateTime.now();
        }

        Invoice invoice = Invoice.builder()
                .invoiceNumber("INV-" + java.util.UUID.randomUUID().toString().substring(0,8))
                .orderId(orderId)
                .amount(order.getTotal())
                .status(initialStatus)
                .createdAt(LocalDateTime.now())
                .issuedAt(LocalDateTime.now())
                .paidAt(paidAt)
                .build();
        return invoice;
    }
}
