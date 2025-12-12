package com.articurated.messaging.consumer;

import com.articurated.order.domain.Order;
import com.articurated.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.articurated.notification.email.EmailSender;
import java.io.File;
import java.math.BigDecimal;

import static org.mockito.Mockito.*;

class InvoiceMessageConsumerTest {

    private OrderService orderService;
    private EmailSender emailSender;
    private InvoiceMessageConsumer consumer;

    @BeforeEach
    void setUp() {
    orderService = mock(OrderService.class);
    emailSender = mock(EmailSender.class);

    // ensure the configured invoices.output.dir points to a temp dir for the test
    String tmp = System.getProperty("java.io.tmpdir") + File.separator + "articurated-test-invoices";
    System.setProperty("invoices.output.dir", tmp);

    consumer = new InvoiceMessageConsumer(orderService, emailSender);
    }

    @Test
    void handleInvoiceGeneration_createsPdfAndSendsEmail() throws Exception {
        Order o = new Order();
        o.setId(101L);
        o.setOrderNumber("ORD-101");
        o.setCustomerEmail("test@example.com");
        o.setTotal(new BigDecimal("10.00"));

        when(orderService.getOrderById(101L)).thenReturn(o);

        consumer.handleInvoiceGeneration(101L);

        verify(orderService, times(1)).getOrderById(101L);
        verify(emailSender, times(1)).sendInvoiceEmail(101L, "test@example.com");

    String tmp = System.getProperty("java.io.tmpdir") + File.separator + "articurated-test-invoices";
    File f = new File(tmp + File.separator + "invoice-101.pdf");
        assert f.exists() : "Expected invoice PDF to exist";

        // verify PDF contains Order ID text
        try (org.apache.pdfbox.pdmodel.PDDocument doc = org.apache.pdfbox.pdmodel.PDDocument.load(f)) {
            String text = new org.apache.pdfbox.text.PDFTextStripper().getText(doc);
            assert text.contains("Order ID: 101");
        }

        // cleanup
        f.delete();
    }
}
