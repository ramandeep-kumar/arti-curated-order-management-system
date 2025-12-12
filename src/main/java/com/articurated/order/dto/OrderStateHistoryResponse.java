package com.articurated.order.dto;

import com.articurated.order.domain.OrderState;
import com.articurated.order.domain.OrderStateHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStateHistoryResponse {
    
    private Long id;
    private OrderState fromState;
    private OrderState toState;
    private String reason;
    private String changedBy;
    private LocalDateTime changedAt;
    
    public static OrderStateHistoryResponse from(OrderStateHistory history) {
        return OrderStateHistoryResponse.builder()
            .id(history.getId())
            .fromState(history.getFromState())
            .toState(history.getToState())
            .reason(history.getReason())
            .changedBy(history.getChangedBy())
            .changedAt(history.getChangedAt())
            .build();
    }
}
