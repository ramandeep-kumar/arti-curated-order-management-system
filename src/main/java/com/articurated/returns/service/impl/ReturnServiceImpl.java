package com.articurated.returns.service.impl;

import com.articurated.order.domain.Order;
import com.articurated.order.domain.OrderState;
import com.articurated.order.service.OrderService;
import com.articurated.returns.domain.Return;
import com.articurated.returns.domain.ReturnState;
import com.articurated.returns.dto.CreateReturnRequest;
import com.articurated.returns.port.ReturnPersistencePort;
import com.articurated.returns.port.ReturnStateHistoryPersistencePort;
import com.articurated.returns.service.ReturnService;
import com.articurated.shared.exception.BusinessException;
import com.articurated.shared.exception.ReturnNotFoundException;
import com.articurated.shared.events.ProcessRefundEvent;
import com.articurated.shared.events.EventPublisher;
import com.articurated.shared.util.NumberGenerator;
import com.articurated.returns.domain.ReturnStateHistory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.articurated.returns.dto.RefundResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Profile("!test")
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReturnServiceImpl implements ReturnService {
    
    private final ReturnPersistencePort returnRepository;
    private final ReturnStateHistoryPersistencePort stateHistoryRepository;
    private final OrderService orderService;
    private final EventPublisher eventPublisher;
    private final NumberGenerator numberGenerator;
    
    @Override
    public Return createReturn(CreateReturnRequest request) {
        log.info("Creating return for order: {}", request.getOrderId());
        
        Order order = orderService.getOrderById(request.getOrderId());
        
        // Validate order is eligible for return
        if (!order.canBeReturned()) {
            throw new BusinessException("Order is not eligible for return. Order must be delivered and within 30 days.");
        }
        
        Return returnEntity = Return.builder()
            .returnNumber(numberGenerator.generate("RET-"))
            .order(order)
            .reason(request.getReason())
            .currentState(ReturnState.REQUESTED)
            .refundAmount(order.getTotal())
            .build();
            
    returnEntity = returnRepository.save(returnEntity);
    recordStateChange(returnEntity, null, ReturnState.REQUESTED, "Return requested by customer");
        
        log.info("Return created successfully with ID: {}", returnEntity.getId());
        return returnEntity;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Return getReturnById(Long returnId) {
        return returnRepository.findByIdWithDetails(returnId)
            .orElseThrow(() -> new ReturnNotFoundException("Return not found with ID: " + returnId));
    }
    
    @Override
    public Return approveReturn(Long returnId, String approvedBy) {
        Return returnEntity = getReturnById(returnId);
        
        if (returnEntity.getCurrentState() != ReturnState.REQUESTED) {
            throw new BusinessException("Return cannot be approved in current state: " + returnEntity.getCurrentState());
        }
        
        returnEntity.setCurrentState(ReturnState.APPROVED);
        returnEntity.setApprovedBy(approvedBy);
    returnEntity = returnRepository.save(returnEntity);
    recordStateChange(returnEntity, ReturnState.REQUESTED, ReturnState.APPROVED, "Return approved by " + approvedBy);
        
        log.info("Return {} approved by {}", returnId, approvedBy);
        return returnEntity;
    }
    
    @Override
    public Return rejectReturn(Long returnId, String rejectedBy) {
        Return returnEntity = getReturnById(returnId);
        
        if (returnEntity.getCurrentState() != ReturnState.REQUESTED) {
            throw new BusinessException("Return cannot be rejected in current state: " + returnEntity.getCurrentState());
        }
        
        returnEntity.setCurrentState(ReturnState.REJECTED);
    returnEntity = returnRepository.save(returnEntity);
    recordStateChange(returnEntity, ReturnState.REQUESTED, ReturnState.REJECTED, "Return rejected by " + rejectedBy);
        
        log.info("Return {} rejected by {}", returnId, rejectedBy);
        return returnEntity;
    }

    @Override
    public Return markInTransit(Long returnId, String trackingNumber) {
        Return returnEntity = getReturnById(returnId);

        if (returnEntity.getCurrentState() != ReturnState.APPROVED) {
            throw new BusinessException("Return cannot be marked IN_TRANSIT from state: " + returnEntity.getCurrentState());
        }

        returnEntity.setCurrentState(ReturnState.IN_TRANSIT);
        returnEntity.setTrackingNumber(trackingNumber);
    returnEntity = returnRepository.save(returnEntity);
    recordStateChange(returnEntity, ReturnState.APPROVED, ReturnState.IN_TRANSIT, "Return marked in transit, tracking=" + trackingNumber);
        log.info("Return {} marked IN_TRANSIT with tracking {}", returnId, trackingNumber);
        return returnEntity;
    }

    @Override
    public Return markReceived(Long returnId) {
        Return returnEntity = getReturnById(returnId);

        if (returnEntity.getCurrentState() != ReturnState.IN_TRANSIT) {
            throw new BusinessException("Return cannot be marked RECEIVED from state: " + returnEntity.getCurrentState());
        }

        returnEntity.setCurrentState(ReturnState.RECEIVED);
    returnEntity = returnRepository.save(returnEntity);
    recordStateChange(returnEntity, ReturnState.IN_TRANSIT, ReturnState.RECEIVED, "Return received by warehouse");
        log.info("Return {} marked RECEIVED", returnId);
        return returnEntity;
    }

    @Override
    public Return completeReturn(Long returnId) {
        Return returnEntity = getReturnById(returnId);

        if (returnEntity.getCurrentState() != ReturnState.RECEIVED) {
            throw new BusinessException("Return cannot be completed from state: " + returnEntity.getCurrentState());
        }

        returnEntity.setCurrentState(ReturnState.COMPLETED);
        returnEntity = returnRepository.save(returnEntity);
        recordStateChange(returnEntity, ReturnState.RECEIVED, ReturnState.COMPLETED, "Return processing completed");
        log.info("Return {} completed", returnId);

        // Publish process refund event after transaction commit so listeners see persisted state
        try {
            eventPublisher.publishAfterCommit(new ProcessRefundEvent(returnId));
        } catch (Exception e) {
            log.warn("Failed to register publish-after-commit for return {}: {}", returnId, e.getMessage());
        }

        return returnEntity;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Return> getReturnsByOrderId(Long orderId) {
        return returnRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public RefundResponse getRefundStatus(Long returnId) {
        Return returnEntity = getReturnById(returnId);

        // Simple simulation: if return is COMPLETED then refund is COMPLETED, else PENDING
        String status = "PENDING";
        LocalDateTime processedAt = null;
        BigDecimal amount = returnEntity.getRefundAmount();

        if (returnEntity.getCurrentState() == ReturnState.COMPLETED) {
            status = "COMPLETED";
            processedAt = LocalDateTime.now();
        }

        return RefundResponse.builder()
            .returnId(returnId)
            .refundStatus(status)
            .amount(amount)
            .processedAt(processedAt)
            .build();
    }
    
    // return number generation moved to NumberGenerator

    // Persist return state history consistently via repository (keeps behavior deterministic)
    private void recordStateChange(Return returnEntity, ReturnState fromState, ReturnState toState, String reason) {
        ReturnStateHistory history = ReturnStateHistory.builder()
            .returnEntity(returnEntity)
            .fromState(fromState)
            .toState(toState)
            .reason(reason)
            .changedBy("SYSTEM")
            .changedAt(LocalDateTime.now())
            .build();
        stateHistoryRepository.save(history);
    }
}
