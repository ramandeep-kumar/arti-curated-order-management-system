package com.articurated.invoice.service.impl;

import com.articurated.invoice.domain.Invoice;
import com.articurated.invoice.domain.InvoiceStatus;
import com.articurated.invoice.port.InvoicePersistencePort;
import com.articurated.invoice.service.InvoiceService;
import com.articurated.order.service.OrderService;
import com.articurated.order.domain.Order;
import com.articurated.shared.exception.BusinessException;
// MessageProducer is used by ApplicationEventHandler; services publish events via EventPublisher
import com.articurated.shared.util.NumberGenerator;
import com.articurated.shared.events.EventPublisher;
import com.articurated.shared.events.GenerateInvoiceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Deprecated
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoicePersistencePort invoiceRepository;
    private final OrderService orderService;
    private final NumberGenerator numberGenerator;
    private final EventPublisher eventPublisher;

    @Override
    public Invoice generateInvoiceForOrder(Long orderId) {
        log.info("Generating invoice for order {}", orderId);

        // If invoice exists for order, return it (policy per your decision)
        Optional<Invoice> existing = invoiceRepository.findByOrderId(orderId);
        if (existing.isPresent()) {
            log.info("Invoice already exists for order {}: {}", orderId, existing.get().getId());
            return existing.get();
        }

        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            throw new BusinessException("Order not found: " + orderId);
        }

        BigDecimal amount = order.getTotal();

        // If the order is already paid (or beyond), create the invoice in PAID state
        InvoiceStatus initialStatus = InvoiceStatus.ISSUED;
        LocalDateTime paidAt = null;
        if (order.getCurrentState() == com.articurated.order.domain.OrderState.PAID
                || order.getCurrentState() == com.articurated.order.domain.OrderState.SHIPPED
                || order.getCurrentState() == com.articurated.order.domain.OrderState.DELIVERED) {
            initialStatus = InvoiceStatus.PAID;
            paidAt = LocalDateTime.now();
        }

        Invoice invoice = Invoice.builder()
            .invoiceNumber(numberGenerator.generate("INV-"))
            .orderId(orderId)
            .amount(amount)
            .status(initialStatus)
            .createdAt(LocalDateTime.now())
            .issuedAt(LocalDateTime.now())
            .paidAt(paidAt)
            .build();

        invoice = invoiceRepository.save(invoice);
        // publish invoice generation event for downstream processing (handled by ApplicationEventHandler)
        try {
            eventPublisher.publishAfterCommit(new GenerateInvoiceEvent(orderId));
        } catch (Exception e) {
            log.warn("Failed to publish invoice generation event for order {}: {}", orderId, e.getMessage());
        }

        return invoice;
    }

    @Override
    @Transactional(readOnly = true)
    public Invoice getInvoiceById(Long invoiceId) {
        return invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new BusinessException("Invoice not found: " + invoiceId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> getInvoicesByOrderId(Long orderId) {
        return invoiceRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
    }

    @Override
    public Invoice markInvoicePaid(Long invoiceId, String paidBy, LocalDateTime paidAt) {
        Invoice invoice = getInvoiceById(invoiceId);
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            return invoice; // idempotent
        }
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BusinessException("Cannot pay a cancelled invoice");
        }
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(paidAt != null ? paidAt : LocalDateTime.now());
        invoice = invoiceRepository.save(invoice);
        return invoice;
    }

    @Override
    public Invoice cancelInvoice(Long invoiceId, String reason) {
        Invoice invoice = getInvoiceById(invoiceId);
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BusinessException("Cannot cancel a paid invoice");
        }
        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoice = invoiceRepository.save(invoice);
        return invoice;
    }

    // invoice number generation moved to NumberGenerator
}
