package com.articurated.returns.service.app;

import com.articurated.returns.domain.Return;
import com.articurated.returns.dto.CreateReturnRequest;

/**
 * Write operations for returns (create/approve/reject/state changes).
 */
public interface ReturnWriteService {
    Return createReturn(CreateReturnRequest request);
    Return approveReturn(Long returnId, String approvedBy);
    Return rejectReturn(Long returnId, String rejectedBy);
    Return markInTransit(Long returnId, String trackingNumber);
    Return markReceived(Long returnId);
    Return completeReturn(Long returnId);
}
