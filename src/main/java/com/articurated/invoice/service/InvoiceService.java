package com.articurated.invoice.service;

import com.articurated.invoice.domain.Invoice;

import java.time.LocalDateTime;
import java.util.List;

public interface InvoiceService {
    Invoice generateInvoiceForOrder(Long orderId);
    Invoice getInvoiceById(Long invoiceId);
    List<Invoice> getInvoicesByOrderId(Long orderId);
    Invoice markInvoicePaid(Long invoiceId, String paidBy, LocalDateTime paidAt);
    Invoice cancelInvoice(Long invoiceId, String reason);
}
