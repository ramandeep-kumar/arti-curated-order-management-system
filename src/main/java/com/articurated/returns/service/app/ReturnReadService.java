package com.articurated.returns.service.app;

import com.articurated.returns.domain.Return;
import com.articurated.returns.dto.RefundResponse;

import java.util.List;

/**
 * Read operations for returns.
 */
public interface ReturnReadService {
    Return getReturnById(Long returnId);
    List<Return> getReturnsByOrderId(Long orderId);
    RefundResponse getRefundStatus(Long returnId);
}
