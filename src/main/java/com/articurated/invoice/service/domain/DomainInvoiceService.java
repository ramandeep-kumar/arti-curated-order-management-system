package com.articurated.invoice.service.domain;

import com.articurated.invoice.domain.Invoice;

public interface DomainInvoiceService {
    Invoice prepareInvoiceForOrder(Long orderId, com.articurated.order.domain.Order order);
}
