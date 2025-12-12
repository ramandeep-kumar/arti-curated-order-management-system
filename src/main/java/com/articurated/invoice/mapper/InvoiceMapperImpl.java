package com.articurated.invoice.mapper;

import com.articurated.invoice.domain.Invoice;
import com.articurated.invoice.dto.InvoiceResponse;
import com.articurated.order.dto.OrderItemResponse;
import com.articurated.order.service.OrderService;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class InvoiceMapperImpl implements InvoiceMapper {
    private OrderService orderService;

    // No-arg constructor for tests and frameworks that construct the mapper directly
    public InvoiceMapperImpl() {
        this.orderService = null;
    }

    public InvoiceMapperImpl(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public InvoiceResponse toResponse(Invoice invoice) {
        if (invoice == null) return null;
        List<OrderItemResponse> items = List.of();
        try {
            var order = orderService.getOrderById(invoice.getOrderId());
            if (order != null && order.getItems() != null) {
                items = order.getItems().stream().map(OrderItemResponse::from).collect(Collectors.toList());
            }
        } catch (Exception ignored) {
        }

        return InvoiceResponse.builder()
            .id(invoice.getId())
            .invoiceNumber(invoice.getInvoiceNumber())
            .orderId(invoice.getOrderId())
            .amount(invoice.getAmount())
            .status(invoice.getStatus() != null ? invoice.getStatus().name() : null)
            .createdAt(invoice.getCreatedAt())
            .issuedAt(invoice.getIssuedAt())
            .paidAt(invoice.getPaidAt())
            .items(items)
            .paid(invoice.getStatus() == com.articurated.invoice.domain.InvoiceStatus.PAID)
            .build();
    }
}
