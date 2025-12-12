package com.articurated.returns.service.domain;

import com.articurated.returns.domain.Return;
import com.articurated.returns.dto.CreateReturnRequest;

public interface DomainReturnService {
    Return createReturnDomain(CreateReturnRequest request);
    Return approveReturnDomain(Return returnEntity, String approvedBy);
    Return rejectReturnDomain(Return returnEntity, String rejectedBy);
    Return markInTransitDomain(Return returnEntity, String trackingNumber);
    Return markReceivedDomain(Return returnEntity);
    Return completeReturnDomain(Return returnEntity);
}
