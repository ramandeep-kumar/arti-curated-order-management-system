package com.articurated.invoice.service.app;

import com.articurated.invoice.domain.Invoice;
import com.articurated.invoice.port.InvoicePersistencePort;
import com.articurated.invoice.service.domain.DomainInvoiceService;
import com.articurated.order.domain.Order;
import com.articurated.order.service.OrderService;
import com.articurated.shared.events.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class InvoiceAppServiceImplTest {

    @Mock
    private InvoicePersistencePort invoiceRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private DomainInvoiceService domainInvoiceService;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private com.articurated.invoice.service.app.InvoiceAppServiceImpl invoiceAppService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = com.articurated.order.domain.Order.builder()
            .id(2L)
            .subtotal(BigDecimal.valueOf(50))
            .tax(BigDecimal.valueOf(5))
            .shipping(BigDecimal.valueOf(2))
            .total(BigDecimal.valueOf(57))
            .build();
    }

    @Test
    void generateInvoice_whenEventPublishThrows_logsWarningButReturnsInvoice() {
        when(invoiceRepository.findByOrderId(anyLong())).thenReturn(Optional.empty());
        when(orderService.getOrderById(2L)).thenReturn(order);
        Invoice invoice = Invoice.builder().orderId(2L).amount(order.getTotal()).build();
        when(domainInvoiceService.prepareInvoiceForOrder(2L, order)).thenReturn(invoice);
        when(invoiceRepository.save(invoice)).thenReturn(invoice);
        doThrow(new RuntimeException("event system down")).when(eventPublisher).publishAfterCommit(Mockito.any());

        Invoice result = invoiceAppService.generateInvoiceForOrder(2L);
        assertThat(result).isNotNull();
        // ensure save was called and exception didn't propagate
        Mockito.verify(invoiceRepository).save(invoice);
    }
}
