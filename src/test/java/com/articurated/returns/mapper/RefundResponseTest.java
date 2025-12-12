package com.articurated.returns.mapper;

import com.articurated.returns.dto.RefundResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RefundResponseTest {

    @Test
    void dto_has_expected_properties() {
        RefundResponse r = RefundResponse.builder()
            .returnId(3L)
            .refundStatus("COMPLETED")
            .amount(new BigDecimal("12.34"))
            .processedAt(LocalDateTime.now().minusHours(1))
            .build();

        assertThat(r.getReturnId()).isEqualTo(3L);
        assertThat(r.getRefundStatus()).isEqualTo("COMPLETED");
        assertThat(r.getAmount()).isEqualTo(new BigDecimal("12.34"));
        assertThat(r.getProcessedAt()).isNotNull();
    }
}
