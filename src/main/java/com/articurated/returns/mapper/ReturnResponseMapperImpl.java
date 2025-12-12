package com.articurated.returns.mapper;

import com.articurated.returns.domain.Return;
import com.articurated.returns.dto.ReturnResponse;
import org.springframework.stereotype.Component;

@Component
public class ReturnResponseMapperImpl implements ReturnResponseMapper {

    @Override
    public ReturnResponse toResponse(Return returnEntity) {
        if (returnEntity == null) return null;

        Long orderId = null;
        if (returnEntity.getOrder() != null) {
            orderId = returnEntity.getOrder().getId();
        }

        return ReturnResponse.builder()
                .id(returnEntity.getId())
                .returnNumber(returnEntity.getReturnNumber())
                .orderId(orderId)
                .reason(returnEntity.getReason())
                .currentState(returnEntity.getCurrentState())
                .approvedBy(returnEntity.getApprovedBy())
                .trackingNumber(returnEntity.getTrackingNumber())
                .refundAmount(returnEntity.getRefundAmount())
                .createdAt(returnEntity.getCreatedAt())
                .build();
    }
}
