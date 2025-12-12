package com.articurated.invoice.controller;

import com.articurated.invoice.dto.CreateInvoiceRequest;
import com.articurated.invoice.dto.InvoiceResponse;
import com.articurated.invoice.domain.Invoice;
import com.articurated.invoice.service.app.InvoiceReadService;
import com.articurated.invoice.service.app.InvoiceWriteService;
import com.articurated.invoice.mapper.InvoiceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceReadService invoiceReadService;
    private final InvoiceWriteService invoiceWriteService;
    private final InvoiceMapper invoiceMapper;
    @Value("${invoices.output.dir:${user.dir}/target/invoices}")
    private String invoicesOutputDir;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<InvoiceResponse> createInvoice(@RequestBody CreateInvoiceRequest req) {
        Invoice invoice = invoiceWriteService.generateInvoiceForOrder(req.getOrderId());
    return ResponseEntity.status(HttpStatus.CREATED).body(invoiceMapper.toResponse(invoice));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getInvoicePdf(@PathVariable Long id) {
        try {
            // ensure invoice exists and compute path
            Invoice invoice = invoiceReadService.getInvoiceById(id);
            java.nio.file.Path pdfPath = java.nio.file.Paths.get(invoicesOutputDir, "invoice-" + invoice.getId() + ".pdf");
            // runtime path check
            if (!java.nio.file.Files.exists(pdfPath)) {
                return ResponseEntity.notFound().build();
            }
            byte[] data = java.nio.file.Files.readAllBytes(pdfPath);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.inline().filename(pdfPath.getFileName().toString()).build());
            headers.setContentLength(data.length);
            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        } catch (com.articurated.shared.exception.BusinessException be) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getInvoice(@PathVariable Long id) {
        Invoice invoice = invoiceReadService.getInvoiceById(id);
    return ResponseEntity.ok(invoiceMapper.toResponse(invoice));
    }

    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByOrder(@RequestParam Long orderId) {
    List<Invoice> invoices = invoiceReadService.getInvoicesByOrderId(orderId);
    List<InvoiceResponse> resp = invoices.stream().map(invoiceMapper::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<InvoiceResponse> payInvoice(@PathVariable Long id, @RequestParam String paidBy) {
    Invoice invoice = invoiceWriteService.markInvoicePaid(id, paidBy, LocalDateTime.now());
    return ResponseEntity.ok(invoiceMapper.toResponse(invoice));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<InvoiceResponse> cancelInvoice(@PathVariable Long id, @RequestParam String reason) {
    Invoice invoice = invoiceWriteService.cancelInvoice(id, reason);
    return ResponseEntity.ok(invoiceMapper.toResponse(invoice));
    }

    // mapping delegated to InvoiceMapper
}
