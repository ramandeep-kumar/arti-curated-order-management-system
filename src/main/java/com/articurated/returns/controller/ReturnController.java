package com.articurated.returns.controller;

import com.articurated.returns.domain.Return;
import com.articurated.returns.dto.CreateReturnRequest;
import com.articurated.returns.dto.ReturnResponse;
import com.articurated.returns.service.ReturnService;
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
import com.articurated.returns.dto.RefundResponse;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
@Validated
@Tag(name = "Returns", description = "Return management operations")
public class ReturnController {
    
    private final com.articurated.returns.service.app.ReturnReadService returnReadService;
    private final com.articurated.returns.service.app.ReturnWriteService returnWriteService;
    private final com.articurated.returns.port.ReturnStateHistoryPersistencePort stateHistoryRepository;
    private final com.articurated.returns.mapper.ReturnResponseMapper returnResponseMapper;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create return request", description = "Create a new return request for a delivered order")
    public ResponseEntity<ReturnResponse> createReturn(@Valid @RequestBody CreateReturnRequest request) {
    Return returnEntity = returnWriteService.createReturn(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(returnResponseMapper.toResponse(returnEntity));
    }
    
    @GetMapping("/{returnId}")
    @Operation(summary = "Get return by ID", description = "Retrieve return request details")
    public ResponseEntity<ReturnResponse> getReturn(@PathVariable Long returnId) {
    Return returnEntity = returnReadService.getReturnById(returnId);
    return ResponseEntity.ok(returnResponseMapper.toResponse(returnEntity));
    }
    
    @PutMapping("/{returnId}/approve")
    @Operation(summary = "Approve return request", description = "Manager approves return request")
    public ResponseEntity<ReturnResponse> approveReturn(
        @PathVariable Long returnId,
        @RequestParam String approvedBy) {
    Return returnEntity = returnWriteService.approveReturn(returnId, approvedBy);
    return ResponseEntity.ok(returnResponseMapper.toResponse(returnEntity));
    }
    
    @PutMapping("/{returnId}/reject")
    @Operation(summary = "Reject return request", description = "Manager rejects return request")
    public ResponseEntity<ReturnResponse> rejectReturn(
        @PathVariable Long returnId,
        @RequestParam String rejectedBy) {
    Return returnEntity = returnWriteService.rejectReturn(returnId, rejectedBy);
    return ResponseEntity.ok(returnResponseMapper.toResponse(returnEntity));
    }
    
    @GetMapping
    @Operation(summary = "Get returns by order", description = "Retrieve returns for a specific order")
    public ResponseEntity<List<ReturnResponse>> getReturnsByOrder(@RequestParam Long orderId) {
        List<Return> returns = returnReadService.getReturnsByOrderId(orderId);
        List<ReturnResponse> response = returns.stream()
            .map(returnResponseMapper::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{returnId}/history")
    @Operation(summary = "Get return state history", description = "Retrieve state change history for a return")
    public ResponseEntity<List<?>> getReturnHistory(@PathVariable Long returnId) {
        // Reuse repository DTO mapping via service - return raw history objects for now
        var histories = stateHistoryRepository.findByReturnEntityIdOrderByChangedAtDesc(returnId);
        return ResponseEntity.ok(histories);
    }

    @PutMapping("/{returnId}/in-transit")
    @Operation(summary = "Mark return as in-transit", description = "Mark a previously approved return as in-transit with tracking number")
    public ResponseEntity<ReturnResponse> markInTransit(
        @PathVariable Long returnId,
        @RequestParam String trackingNumber) {
    Return returnEntity = returnWriteService.markInTransit(returnId, trackingNumber);
    return ResponseEntity.ok(returnResponseMapper.toResponse(returnEntity));
    }

    @PutMapping("/{returnId}/received")
    @Operation(summary = "Mark return as received", description = "Warehouse marks return as received")
    public ResponseEntity<ReturnResponse> markReceived(@PathVariable Long returnId) {
    Return returnEntity = returnWriteService.markReceived(returnId);
    return ResponseEntity.ok(returnResponseMapper.toResponse(returnEntity));
    }

    @PutMapping("/{returnId}/complete")
    @Operation(summary = "Complete return processing", description = "Complete return processing and trigger refund")
    public ResponseEntity<ReturnResponse> completeReturn(@PathVariable Long returnId) {
    Return returnEntity = returnWriteService.completeReturn(returnId);
    return ResponseEntity.ok(returnResponseMapper.toResponse(returnEntity));
    }

    @GetMapping("/{returnId}/refund")
    @Operation(summary = "Get refund status for a return", description = "Retrieve refund status and metadata for a completed or pending refund")
    public ResponseEntity<RefundResponse> getRefundStatus(@PathVariable Long returnId) {
        RefundResponse resp = returnReadService.getRefundStatus(returnId);
        return ResponseEntity.ok(resp);
    }

    // Convenience nested endpoint to create a return using a path orderId
    @PostMapping("/orders/{orderId}/returns")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a return for an order (nested)")
    public ResponseEntity<ReturnResponse> createReturnForOrder(
        @PathVariable Long orderId,
        @Valid @RequestBody CreateReturnRequest request) {
        // ensure request.orderId matches path or inject it
        if (request.getOrderId() == null) {
            request.setOrderId(orderId);
        }
    Return returnEntity = returnWriteService.createReturn(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(returnResponseMapper.toResponse(returnEntity));
    }
}
