package com.articurated.invoice.service.app;

import com.articurated.invoice.domain.Invoice;

/**
 * Write operations for invoices (generate/pay/cancel).
 */
public interface InvoiceWriteService {
    Invoice generateInvoiceForOrder(Long orderId);
    Invoice markInvoicePaid(Long invoiceId, String paidBy, java.time.LocalDateTime paidAt);
    Invoice cancelInvoice(Long invoiceId, String reason);
}
