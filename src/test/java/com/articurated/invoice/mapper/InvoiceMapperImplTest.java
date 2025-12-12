package com.articurated.invoice.mapper;

import com.articurated.invoice.domain.Invoice;
import com.articurated.invoice.dto.InvoiceResponse;
import com.articurated.invoice.domain.InvoiceStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceMapperImplTest {

    private final InvoiceMapperImpl mapper = new InvoiceMapperImpl();

    @Test
    void toResponse_maps_fields() {
        Invoice inv = Invoice.builder()
            .id(42L)
            .invoiceNumber("INV-42")
            .orderId(2L)
            .amount(new BigDecimal("42.00"))
            .status(InvoiceStatus.ISSUED)
            .createdAt(LocalDateTime.now().minusDays(1))
            .issuedAt(LocalDateTime.now().minusHours(6))
            .paidAt(null)
            .build();

        InvoiceResponse resp = mapper.toResponse(inv);

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isEqualTo(inv.getId());
        assertThat(resp.getInvoiceNumber()).isEqualTo(inv.getInvoiceNumber());
        assertThat(resp.getOrderId()).isEqualTo(inv.getOrderId());
        assertThat(resp.getAmount()).isEqualTo(inv.getAmount());
        assertThat(resp.getStatus()).isEqualTo(inv.getStatus().name());
    }
}
