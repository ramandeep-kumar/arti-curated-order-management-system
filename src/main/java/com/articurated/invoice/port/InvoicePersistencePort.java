package com.articurated.invoice.port;

import com.articurated.invoice.domain.Invoice;

import java.util.List;
import java.util.Optional;

public interface InvoicePersistencePort {
    Invoice save(Invoice invoice);
    Optional<Invoice> findById(Long id);
    Optional<Invoice> findByOrderId(Long orderId);
    List<Invoice> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}
