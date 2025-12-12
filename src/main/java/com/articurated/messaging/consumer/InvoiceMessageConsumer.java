package com.articurated.messaging.consumer;

import com.articurated.order.service.OrderService;
import com.articurated.notification.email.EmailSender;
import com.articurated.shared.config.RabbitMQConfig;
import com.articurated.messaging.util.MessagingMdc;
import org.springframework.beans.factory.annotation.Value;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceMessageConsumer {
    
    private final OrderService orderService;
    private final EmailSender emailSender;
    // configurable output directory for generated PDFs; default falls back to target/invoices so tests that
    // construct the consumer directly still work.
    @Value("${invoices.output.dir:${user.dir}/target/invoices}")
    // initialize from system property when the class is constructed directly (unit tests)
    private String invoiceOutputDir = System.getProperty("invoices.output.dir", System.getProperty("user.dir") + File.separator + "target" + File.separator + "invoices");

    // optional meter registry; wired when present (not required to run unit tests)
    private MeterRegistry meterRegistry;

    @Autowired(required = false)
    public void setMeterRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    // no PostConstruct required; field initialization reads system property for direct construction
    
    @RabbitListener(queues = RabbitMQConfig.INVOICE_QUEUE)
    public void handleInvoiceGeneration(Message<?> message) {
        Object raw = message.getPayload();
        Long orderId = null;
        if (raw instanceof Number) {
            orderId = ((Number) raw).longValue();
        } else if (raw != null) {
            orderId = Long.parseLong(raw.toString());
        }
        org.springframework.amqp.core.Message amqpMessage = (org.springframework.amqp.core.Message) message.getHeaders().get("amqp_receivedMessage");
        String correlationId = null;
        String timestamp = null;
        if (amqpMessage != null) {
            var props = amqpMessage.getMessageProperties();
            Object cid = props.getHeaders() != null ? props.getHeaders().get("correlationId") : null;
            Object ts = props.getHeaders() != null ? props.getHeaders().get("timestamp") : null;
            correlationId = cid != null ? cid.toString() : null;
            timestamp = ts != null ? ts.toString() : null;
        }
        MessagingMdc.putCorrelationId(correlationId);
        MessagingMdc.putMessageTimestamp(timestamp);
        log.info("Processing invoice generation for order: {} (correlationId={}, timestamp={})", orderId, correlationId, timestamp);

        try {
            // Get order details
            var order = orderService.getOrderById(orderId);

            // Generate PDF invoice (actual small PDF)
            generateDummyPDF(orderId, order.getOrderNumber());

            // Send invoice email via EmailSender
            emailSender.sendInvoiceEmail(orderId, order.getCustomerEmail());

            log.info("Invoice generated and sent successfully for order: {}", orderId);

        } catch (Exception e) {
            log.error("Failed to generate invoice for order: {}", orderId, e);
            // In production, you might want to send to a dead letter queue
            // or implement retry logic
            throw new RuntimeException(e);
        } finally {
            MessagingMdc.clear();
        }
    }

    // convenience overload used by unit tests and internal callers
    public void handleInvoiceGeneration(Long orderId) {
        handleInvoiceGeneration(new org.springframework.messaging.support.GenericMessage<>(orderId));
    }
    
    private void generateDummyPDF(Long orderId, String orderNumber) {
        log.info("Generating PDF invoice for order: {} ({})", orderId, orderNumber);

        try {
            Path outDir = Paths.get(invoiceOutputDir);
            if (!Files.exists(outDir)) Files.createDirectories(outDir);

            String filename = String.format("invoice-%d.pdf", orderId);
            Path out = outDir.resolve(filename);

            try (PDDocument doc = new PDDocument()) {
                PDPage page = new PDPage(PDRectangle.LETTER);
                doc.addPage(page);

                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
                    cs.newLineAtOffset(50, 700);
                    cs.showText("Invoice");
                    cs.endText();

                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA, 12);
                    cs.newLineAtOffset(50, 660);
                    cs.showText("Order ID: " + orderId);
                    cs.newLineAtOffset(0, -15);
                    cs.showText("Order Number: " + (orderNumber != null ? orderNumber : "N/A"));
                    cs.endText();
                }

                doc.save(out.toFile());
            }

            log.info("PDF invoice written to {} for order: {}", out.toAbsolutePath(), orderId);

            if (meterRegistry != null) {
                try {
                    meterRegistry.counter("invoice.generated").increment();
                } catch (Exception ignored) {
                    // don't let metrics failures affect processing
                }
            }

        } catch (IOException e) {
            log.error("Failed to generate PDF invoice for order: {}", orderId, e);
        }
    }
    
    // email sending delegated to EmailSender
}
