package com.articurated.invoice.service.app;

import com.articurated.invoice.domain.Invoice;
import java.util.List;

/**
 * Read operations for invoices.
 */
public interface InvoiceReadService {
    Invoice getInvoiceById(Long invoiceId);
    List<Invoice> getInvoicesByOrderId(Long orderId);
}
