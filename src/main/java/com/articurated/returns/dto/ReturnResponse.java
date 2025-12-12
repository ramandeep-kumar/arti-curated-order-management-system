package com.articurated.returns.dto;

import com.articurated.returns.domain.ReturnState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnResponse {
    
    private Long id;
    private String returnNumber;
    private Long orderId;
    private String reason;
    private ReturnState currentState;
    private String approvedBy;
    private String trackingNumber;
    private BigDecimal refundAmount;
    private LocalDateTime createdAt;
    
    // Static factory removed: use `com.articurated.returns.mapper.ReturnResponseMapper#toResponse(Return)` instead
}
