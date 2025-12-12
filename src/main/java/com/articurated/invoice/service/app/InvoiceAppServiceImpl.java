package com.articurated.invoice.service.app;

import com.articurated.invoice.domain.Invoice;
import com.articurated.invoice.port.InvoicePersistencePort;
import com.articurated.invoice.service.InvoiceService;
import com.articurated.order.service.OrderService;
import com.articurated.invoice.service.domain.DomainInvoiceService;
import com.articurated.shared.events.EventPublisher;
import com.articurated.shared.events.GenerateInvoiceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class InvoiceAppServiceImpl implements InvoiceService, com.articurated.invoice.service.app.InvoiceReadService, com.articurated.invoice.service.app.InvoiceWriteService {

    private final InvoicePersistencePort invoiceRepository;
    private final OrderService orderService;
    private final DomainInvoiceService domainInvoiceService;
    private final EventPublisher eventPublisher;

    @Override
    public Invoice generateInvoiceForOrder(Long orderId) {
        Optional<Invoice> existing = invoiceRepository.findByOrderId(orderId);
        if (existing.isPresent()) return existing.get();
        com.articurated.order.domain.Order order = orderService.getOrderById(orderId);
        if (order == null) throw new com.articurated.shared.exception.BusinessException("Order not found: " + orderId);
        Invoice invoice = domainInvoiceService.prepareInvoiceForOrder(orderId, order);
        invoice = invoiceRepository.save(invoice);
        try {
            eventPublisher.publishAfterCommit(new GenerateInvoiceEvent(orderId));
        } catch (Exception e) {
            log.warn("Failed to publish invoice generation event for order {}: {}", orderId, e.getMessage());
        }
        return invoice;
    }

    @Override
    public Invoice getInvoiceById(Long invoiceId) {
        return invoiceRepository.findById(invoiceId).orElseThrow(() -> new com.articurated.shared.exception.BusinessException("Invoice not found: " + invoiceId));
    }

    @Override
    public List<Invoice> getInvoicesByOrderId(Long orderId) {
        return invoiceRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
    }

    @Override
    public Invoice markInvoicePaid(Long invoiceId, String paidBy, java.time.LocalDateTime paidAt) {
        Invoice invoice = getInvoiceById(invoiceId);
        if (invoice.getStatus() == com.articurated.invoice.domain.InvoiceStatus.PAID) return invoice;
        if (invoice.getStatus() == com.articurated.invoice.domain.InvoiceStatus.CANCELLED) throw new com.articurated.shared.exception.BusinessException("Cannot pay a cancelled invoice");
        invoice.setStatus(com.articurated.invoice.domain.InvoiceStatus.PAID);
        invoice.setPaidAt(paidAt != null ? paidAt : java.time.LocalDateTime.now());
        invoice = invoiceRepository.save(invoice);
        return invoice;
    }

    @Override
    public Invoice cancelInvoice(Long invoiceId, String reason) {
        Invoice invoice = getInvoiceById(invoiceId);
        if (invoice.getStatus() == com.articurated.invoice.domain.InvoiceStatus.PAID) throw new com.articurated.shared.exception.BusinessException("Cannot cancel a paid invoice");
        invoice.setStatus(com.articurated.invoice.domain.InvoiceStatus.CANCELLED);
        invoice = invoiceRepository.save(invoice);
        return invoice;
    }
}
