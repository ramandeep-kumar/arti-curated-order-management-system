package com.articurated.returns.service;

import com.articurated.returns.domain.Return;
import com.articurated.returns.dto.CreateReturnRequest;
import com.articurated.returns.dto.RefundResponse;

import java.util.List;

public interface ReturnService {
    Return createReturn(CreateReturnRequest request);
    Return getReturnById(Long returnId);
    Return approveReturn(Long returnId, String approvedBy);
    Return rejectReturn(Long returnId, String rejectedBy);
    Return markInTransit(Long returnId, String trackingNumber);
    Return markReceived(Long returnId);
    Return completeReturn(Long returnId);
    RefundResponse getRefundStatus(Long returnId);
    List<Return> getReturnsByOrderId(Long orderId);
}
