package com.articurated.returns.dto;

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
public class RefundResponse {
    private Long returnId;
    private String refundStatus; // PENDING, PROCESSING, COMPLETED, FAILED
    private BigDecimal amount;
    private LocalDateTime processedAt;
}
