package com.articurated.invoice.adapter.persistence;

import com.articurated.invoice.domain.Invoice;
import com.articurated.invoice.port.InvoicePersistencePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(SpringDataInvoicePersistenceAdapter.class)
public class InvoicePersistenceAdapterIT {

    @Autowired
    private InvoicePersistencePort port;

    @Test
    void saveAndFindById_shouldPersistAndRetrieveInvoice() {
        Invoice inv = Invoice.builder()
            .invoiceNumber("IT-INV-1")
            .orderId(1L)
            .amount(new BigDecimal("10.00"))
            .status(com.articurated.invoice.domain.InvoiceStatus.ISSUED)
            .build();

        Invoice saved = port.save(inv);
        assertThat(saved.getId()).isNotNull();

        Invoice found = port.findById(saved.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getInvoiceNumber()).isEqualTo("IT-INV-1");
    }
}
