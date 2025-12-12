package com.articurated.returns.service.app;

import com.articurated.order.domain.Order;
import com.articurated.order.service.OrderService;
import com.articurated.returns.domain.Return;
import com.articurated.returns.domain.ReturnState;
import com.articurated.returns.dto.CreateReturnRequest;
import com.articurated.returns.dto.RefundResponse;
import com.articurated.returns.port.ReturnPersistencePort;
import com.articurated.statetransition.StateTransitionHandlerRegistry;
import com.articurated.returns.service.ReturnService;
import com.articurated.returns.service.domain.DomainReturnService;
import com.articurated.shared.util.NumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Primary;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Primary
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReturnAppServiceImpl implements ReturnService, com.articurated.returns.service.app.ReturnReadService, com.articurated.returns.service.app.ReturnWriteService {

    private final ReturnPersistencePort returnRepository;
    private final OrderService orderService;
    private final DomainReturnService domainReturnService;
    private final NumberGenerator numberGenerator;
    private final StateTransitionHandlerRegistry handlerRegistry;

    @Override
    public Return createReturn(CreateReturnRequest request) {
        log.info("Creating return for order: {}", request.getOrderId());
        Order order = orderService.getOrderById(request.getOrderId());
        if (!order.canBeReturned()) {
            throw new com.articurated.shared.exception.BusinessException("Order is not eligible for return. Order must be delivered and within 30 days.");
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
    public Return getReturnById(Long returnId) {
        return returnRepository.findByIdWithDetails(returnId)
            .orElseThrow(() -> new com.articurated.shared.exception.ReturnNotFoundException("Return not found with ID: " + returnId));
    }

    @Override
    public Return approveReturn(Long returnId, String approvedBy) {
        Return r = getReturnById(returnId);
        Return updated = domainReturnService.approveReturnDomain(r, approvedBy);
        updated = returnRepository.save(updated);
        recordStateChange(updated, ReturnState.REQUESTED, ReturnState.APPROVED, "Return approved by " + approvedBy);
        return updated;
    }

    @Override
    public Return rejectReturn(Long returnId, String rejectedBy) {
        Return r = getReturnById(returnId);
        Return updated = domainReturnService.rejectReturnDomain(r, rejectedBy);
        updated = returnRepository.save(updated);
        recordStateChange(updated, ReturnState.REQUESTED, ReturnState.REJECTED, "Return rejected by " + rejectedBy);
        return updated;
    }

    @Override
    public Return markInTransit(Long returnId, String trackingNumber) {
        Return r = getReturnById(returnId);
        Return updated = domainReturnService.markInTransitDomain(r, trackingNumber);
        updated = returnRepository.save(updated);
        recordStateChange(updated, ReturnState.APPROVED, ReturnState.IN_TRANSIT, "Return marked in transit, tracking=" + trackingNumber);
        return updated;
    }

    @Override
    public Return markReceived(Long returnId) {
        Return r = getReturnById(returnId);
        Return updated = domainReturnService.markReceivedDomain(r);
        updated = returnRepository.save(updated);
        recordStateChange(updated, ReturnState.IN_TRANSIT, ReturnState.RECEIVED, "Return received by warehouse");
        return updated;
    }

    @Override
    public Return completeReturn(Long returnId) {
        Return r = getReturnById(returnId);
        Return updated = domainReturnService.completeReturnDomain(r);
        updated = returnRepository.save(updated);
        recordStateChange(updated, ReturnState.RECEIVED, ReturnState.COMPLETED, "Return processing completed");
        log.info("Return {} completed", returnId);
        // event already emitted by domain in this implementation; if not, emit here
        return updated;
    }

    @Override
    public List<Return> getReturnsByOrderId(Long orderId) {
        return returnRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
    }

    @Override
    public RefundResponse getRefundStatus(Long returnId) {
        Return returnEntity = getReturnById(returnId);

        String status = "PENDING";
        LocalDateTime processedAt = null;
        java.math.BigDecimal amount = returnEntity.getRefundAmount();

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

    private void recordStateChange(Return returnEntity, ReturnState fromState, ReturnState toState, String reason) {
        // Delegate to handlers for pluggable side-effects
        for (var h : handlerRegistry.getHandlers()) {
            try {
                @SuppressWarnings("unchecked")
                com.articurated.statetransition.StateTransitionHandler<com.articurated.returns.domain.Return, com.articurated.returns.domain.ReturnState> handler = (com.articurated.statetransition.StateTransitionHandler<com.articurated.returns.domain.Return, com.articurated.returns.domain.ReturnState>) h;
                handler.handle(returnEntity, fromState, toState, reason);
            } catch (ClassCastException ex) {
                // not applicable
            } catch (Exception e) {
                // swallow
            }
        }
    }
}
