package com.articurated.order.mapper;

import com.articurated.order.domain.OrderState;
import com.articurated.order.domain.OrderStateHistory;
import com.articurated.order.dto.OrderStateHistoryResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStateHistoryResponseTest {

    @Test
    void from_maps_history_to_response() {
        OrderStateHistory h = OrderStateHistory.builder()
            .id(5L)
            .fromState(OrderState.PENDING_PAYMENT)
            .toState(OrderState.PAID)
            .reason("paid by user")
            .changedBy("tester")
            .changedAt(LocalDateTime.now().minusHours(1))
            .build();

        OrderStateHistoryResponse resp = OrderStateHistoryResponse.from(h);

        assertThat(resp.getId()).isEqualTo(h.getId());
        assertThat(resp.getFromState()).isEqualTo(h.getFromState());
        assertThat(resp.getToState()).isEqualTo(h.getToState());
        assertThat(resp.getReason()).isEqualTo(h.getReason());
        assertThat(resp.getChangedBy()).isEqualTo(h.getChangedBy());
    }
}
