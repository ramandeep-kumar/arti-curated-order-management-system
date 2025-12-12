package com.articurated.returns.service.domain;

import com.articurated.returns.domain.Return;
import com.articurated.returns.domain.ReturnState;
import com.articurated.returns.dto.CreateReturnRequest;
import com.articurated.shared.events.ProcessRefundEvent;
import com.articurated.shared.events.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DomainReturnServiceImpl implements DomainReturnService {

    private final EventPublisher eventPublisher;

    @Override
    public Return createReturnDomain(CreateReturnRequest request) {
        // Domain validation and entity construction should be done by caller of this domain service
        throw new UnsupportedOperationException("Use application service to create return");
    }

    @Override
    public Return approveReturnDomain(Return returnEntity, String approvedBy) {
        if (returnEntity.getCurrentState() != ReturnState.REQUESTED) {
            throw new IllegalStateException("Return cannot be approved in current state");
        }
        returnEntity.setCurrentState(ReturnState.APPROVED);
        returnEntity.setApprovedBy(approvedBy);
        return returnEntity;
    }

    @Override
    public Return rejectReturnDomain(Return returnEntity, String rejectedBy) {
        if (returnEntity.getCurrentState() != ReturnState.REQUESTED) {
            throw new IllegalStateException("Return cannot be rejected in current state");
        }
        returnEntity.setCurrentState(ReturnState.REJECTED);
        return returnEntity;
    }

    @Override
    public Return markInTransitDomain(Return returnEntity, String trackingNumber) {
        if (returnEntity.getCurrentState() != ReturnState.APPROVED) {
            throw new IllegalStateException("Return cannot be marked IN_TRANSIT from state: " + returnEntity.getCurrentState());
        }
        returnEntity.setCurrentState(ReturnState.IN_TRANSIT);
        returnEntity.setTrackingNumber(trackingNumber);
        return returnEntity;
    }

    @Override
    public Return markReceivedDomain(Return returnEntity) {
        if (returnEntity.getCurrentState() != ReturnState.IN_TRANSIT) {
            throw new IllegalStateException("Return cannot be marked RECEIVED from state: " + returnEntity.getCurrentState());
        }
        returnEntity.setCurrentState(ReturnState.RECEIVED);
        return returnEntity;
    }

    @Override
    public Return completeReturnDomain(Return returnEntity) {
        if (returnEntity.getCurrentState() != ReturnState.RECEIVED) {
            throw new IllegalStateException("Return cannot be completed from state: " + returnEntity.getCurrentState());
        }
        returnEntity.setCurrentState(ReturnState.COMPLETED);
        // domain may emit an event for refund processing
        try {
            eventPublisher.publishAfterCommit(new ProcessRefundEvent(returnEntity.getId()));
        } catch (Exception e) {
            // swallow; application service may decide on retries
        }
        return returnEntity;
    }
}
