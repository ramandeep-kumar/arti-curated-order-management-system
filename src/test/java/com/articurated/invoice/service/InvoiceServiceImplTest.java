package com.articurated.invoice.service;

import com.articurated.invoice.domain.Invoice;
import com.articurated.invoice.domain.InvoiceStatus;
// ...existing imports...
import com.articurated.invoice.service.impl.InvoiceServiceImpl;
import com.articurated.order.domain.Order;
import com.articurated.order.service.OrderService;
import com.articurated.shared.events.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InvoiceServiceImplTest {

    @Mock
    com.articurated.invoice.port.InvoicePersistencePort invoiceRepository;

    @Mock
    OrderService orderService;

    @Mock
    EventPublisher eventPublisher;

    @Mock
    com.articurated.shared.util.NumberGenerator numberGenerator;

    @InjectMocks
    InvoiceServiceImpl invoiceService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    org.mockito.Mockito.lenient().when(numberGenerator.generate(anyString())).thenReturn("INV-TEST-1");
    }

    @Test
    void generateInvoice_returnsExisting_ifPresent() {
        Order order = new Order();
        order.setId(1L);
        order.setTotal(new BigDecimal("10.00"));

        Invoice existing = Invoice.builder()
            .id(5L)
            .invoiceNumber("INV-EXIST")
            .orderId(1L)
            .amount(new BigDecimal("10.00"))
            .status(InvoiceStatus.ISSUED)
            .createdAt(LocalDateTime.now())
            .build();

        when(invoiceRepository.findByOrderId(1L)).thenReturn(Optional.of(existing));

        Invoice result = invoiceService.generateInvoiceForOrder(1L);

        assertEquals(existing.getId(), result.getId());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void generateInvoice_createsNew_whenNotExists() {
        Order order = new Order();
        order.setId(2L);
        order.setTotal(new BigDecimal("25.00"));

        when(invoiceRepository.findByOrderId(2L)).thenReturn(Optional.empty());
        when(orderService.getOrderById(2L)).thenReturn(order);

    when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    Invoice result = invoiceService.generateInvoiceForOrder(2L);

    assertNotNull(result.getInvoiceNumber());
    assertEquals(new BigDecimal("25.00"), result.getAmount());
    verify(eventPublisher, times(1)).publishAfterCommit(any());
    }

    @Test
    void generateInvoice_marksPaid_whenOrderAlreadyPaid() {
        Order order = new Order();
        order.setId(3L);
        order.setTotal(new BigDecimal("50.00"));
        order.setCurrentState(com.articurated.order.domain.OrderState.PAID);

        when(invoiceRepository.findByOrderId(3L)).thenReturn(Optional.empty());
        when(orderService.getOrderById(3L)).thenReturn(order);
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Invoice result = invoiceService.generateInvoiceForOrder(3L);

        assertEquals(InvoiceStatus.PAID, result.getStatus());
        assertNotNull(result.getPaidAt());
        verify(eventPublisher, times(1)).publishAfterCommit(any());
    }
}
