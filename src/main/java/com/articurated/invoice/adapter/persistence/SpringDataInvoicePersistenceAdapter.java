package com.articurated.invoice.adapter.persistence;

import com.articurated.invoice.domain.Invoice;
import com.articurated.invoice.port.InvoicePersistencePort;
import com.articurated.invoice.repository.InvoiceRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SpringDataInvoicePersistenceAdapter implements InvoicePersistencePort {

    private final InvoiceRepository invoiceRepository;

    public SpringDataInvoicePersistenceAdapter(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    public Invoice save(Invoice invoice) {
        return invoiceRepository.save(invoice);
    }

    @Override
    public Optional<Invoice> findById(Long id) {
        return invoiceRepository.findById(id);
    }

    @Override
    public Optional<Invoice> findByOrderId(Long orderId) {
        return invoiceRepository.findByOrderId(orderId);
    }

    @Override
    public List<Invoice> findByOrderIdOrderByCreatedAtDesc(Long orderId) {
        return invoiceRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
    }
}
