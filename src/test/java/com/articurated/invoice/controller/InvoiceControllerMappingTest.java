package com.articurated.invoice.controller;

import com.articurated.invoice.domain.Invoice;
import com.articurated.invoice.dto.InvoiceResponse;
import com.articurated.invoice.service.InvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class InvoiceControllerMappingTest {

    @Mock
    private com.articurated.invoice.service.app.InvoiceReadService invoiceService;

    @Mock
    private com.articurated.invoice.service.app.InvoiceWriteService invoiceWriteService;

    private InvoiceController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    controller = new InvoiceController(invoiceService, invoiceWriteService, new com.articurated.invoice.mapper.InvoiceMapperImpl());
    }

    @Test
    void toResponse_is_used_when_getting_invoice() {
        Invoice inv = Invoice.builder()
            .id(99L)
            .invoiceNumber("INV-99")
            .orderId(55L)
            .amount(new BigDecimal("100.00"))
            .status(com.articurated.invoice.domain.InvoiceStatus.ISSUED)
            .createdAt(LocalDateTime.now().minusDays(1))
            .issuedAt(LocalDateTime.now().minusHours(10))
            .paidAt(null)
            .build();

        when(invoiceService.getInvoiceById(99L)).thenReturn(inv);

        ResponseEntity<InvoiceResponse> resp = controller.getInvoice(99L);

        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
        InvoiceResponse body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getId()).isEqualTo(inv.getId());
        assertThat(body.getInvoiceNumber()).isEqualTo(inv.getInvoiceNumber());
        assertThat(body.getOrderId()).isEqualTo(inv.getOrderId());
        assertThat(body.getAmount()).isEqualTo(inv.getAmount());
        assertThat(body.getStatus()).isEqualTo(inv.getStatus().name());
    }
}
