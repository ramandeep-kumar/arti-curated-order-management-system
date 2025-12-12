package com.articurated.invoice.controller;

import com.articurated.invoice.dto.CreateInvoiceRequest;
import com.articurated.invoice.domain.Invoice;
import com.articurated.invoice.domain.InvoiceStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class InvoiceControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private com.articurated.invoice.service.app.InvoiceReadService invoiceReadService;

    @Mock
    private com.articurated.invoice.service.app.InvoiceWriteService invoiceWriteService;

    @Mock
    private com.articurated.invoice.mapper.InvoiceMapper invoiceMapper;

    @InjectMocks
    private InvoiceController invoiceController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(invoiceController).build();
    }

    @Test
    void createInvoice_returnsCreated() throws Exception {
        CreateInvoiceRequest req = new CreateInvoiceRequest();
        req.setOrderId(1L);

        Invoice invoice = Invoice.builder()
            .id(123L)
            .invoiceNumber("INV-123")
            .orderId(1L)
            .amount(new java.math.BigDecimal("10.00"))
            .status(InvoiceStatus.ISSUED)
            .createdAt(java.time.LocalDateTime.now())
            .build();

    when(invoiceWriteService.generateInvoiceForOrder(1L)).thenReturn(invoice);

        mockMvc.perform(post("/api/invoices")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated());
    }
}
