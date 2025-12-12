package com.articurated.order.controller;

import com.articurated.order.domain.Order;
import com.articurated.order.domain.OrderEvent;
import com.articurated.order.domain.OrderStateHistory;
import com.articurated.order.dto.CreateOrderRequest;
import com.articurated.order.dto.OrderResponse;
import com.articurated.order.dto.OrderStateHistoryResponse;
import com.articurated.order.service.app.OrderReadService;
import com.articurated.order.service.app.OrderWriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
@Tag(name = "Orders", description = "Order management operations")
public class OrderController {
    
    private final OrderReadService orderReadService;
    private final OrderWriteService orderWriteService;
    private final com.articurated.order.mapper.OrderResponseMapper orderResponseMapper;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new order", description = "Creates a new order in PENDING_PAYMENT state")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderWriteService.createOrder(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(orderResponseMapper.toResponse(order));
    }

    @PostMapping("/{orderId}/items")
    @Operation(summary = "Append items to an existing order", description = "Add items to an existing order and recalculate totals",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Array of order item requests",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.articurated.order.dto.OrderItemRequest.class)),
                examples = {
                    @io.swagger.v3.oas.annotations.media.ExampleObject(name = "items", value = "[{ \"productName\": \"Extra Widget\", \"price\": 19.99, \"quantity\": 2 }]" )
                }
            )
        )
    )
    public ResponseEntity<OrderResponse> addItemsToOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody List<com.articurated.order.dto.OrderItemRequest> items) {
        Order order = orderWriteService.addItems(orderId, items);
        return ResponseEntity.ok(orderResponseMapper.toResponse(order));
    }
    
    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Retrieve a specific order with full details")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        Order order = orderReadService.getOrderById(orderId);
    return ResponseEntity.ok(orderResponseMapper.toResponse(order));
    }
    
    @PutMapping("/{orderId}/transition")
    @Operation(summary = "Transition order state", description = "Manually transition order to next state")
    public ResponseEntity<OrderResponse> transitionOrder(
            @PathVariable Long orderId,
            @RequestParam OrderEvent event) {
        Order order = orderWriteService.transitionOrderState(orderId, event);
    return ResponseEntity.ok(orderResponseMapper.toResponse(order));
    }
    
    @GetMapping
    @Operation(summary = "Get orders by customer", description = "Retrieve orders for a specific customer")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(
            @RequestParam String customerEmail) {
        List<Order> orders = orderReadService.getOrdersByCustomerEmail(customerEmail);
        List<OrderResponse> response = orders.stream()
            .map(orderResponseMapper::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{orderId}/history")
    @Operation(summary = "Get order state history", description = "Retrieve complete state change history for an order")
    public ResponseEntity<List<OrderStateHistoryResponse>> getOrderHistory(@PathVariable Long orderId) {
        List<OrderStateHistory> history = orderReadService.getOrderHistory(orderId);
        List<OrderStateHistoryResponse> response = history.stream()
            .map(OrderStateHistoryResponse::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // --- Convenience endpoints for common state transitions (Phase 1)
    @PostMapping("/{orderId}/pay")
    @Operation(summary = "Mark order as paid")
    public ResponseEntity<OrderResponse> payOrder(@PathVariable Long orderId) {
    Order order = orderWriteService.transitionOrderState(orderId, OrderEvent.PAYMENT_RECEIVED);
    return ResponseEntity.ok(orderResponseMapper.toResponse(order));
    }

    @PostMapping("/{orderId}/start-processing")
    @Operation(summary = "Start processing order")
    public ResponseEntity<OrderResponse> startProcessing(@PathVariable Long orderId) {
    Order order = orderWriteService.transitionOrderState(orderId, OrderEvent.START_PROCESSING);
    return ResponseEntity.ok(orderResponseMapper.toResponse(order));
    }

    @PostMapping("/{orderId}/ship")
    @Operation(summary = "Mark order as shipped")
    public ResponseEntity<OrderResponse> shipOrder(@PathVariable Long orderId) {
    Order order = orderWriteService.transitionOrderState(orderId, OrderEvent.SHIP_ORDER);
    return ResponseEntity.ok(orderResponseMapper.toResponse(order));
    }

    @PostMapping("/{orderId}/deliver")
    @Operation(summary = "Mark order as delivered")
    public ResponseEntity<OrderResponse> deliverOrder(@PathVariable Long orderId) {
    Order order = orderWriteService.transitionOrderState(orderId, OrderEvent.DELIVER_ORDER);
    return ResponseEntity.ok(orderResponseMapper.toResponse(order));
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long orderId) {
    Order order = orderWriteService.transitionOrderState(orderId, OrderEvent.CANCEL_ORDER);
    return ResponseEntity.ok(orderResponseMapper.toResponse(order));
    }
}
