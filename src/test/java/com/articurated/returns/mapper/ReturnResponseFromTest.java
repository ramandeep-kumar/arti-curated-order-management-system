package com.articurated.returns.mapper;

import com.articurated.order.domain.Order;
import com.articurated.returns.domain.Return;
import com.articurated.returns.dto.ReturnResponse;
import com.articurated.returns.domain.ReturnState;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReturnResponseFromTest {

    @Test
    void mapper_maps_return_to_response() {
        Order order = Order.builder().id(22L).build();

        Return r = Return.builder()
            .id(2L)
            .returnNumber("R-22")
            .order(order)
            .reason("defective")
            .currentState(ReturnState.REQUESTED)
            .approvedBy("manager")
            .trackingNumber("TRACK123")
            .refundAmount(new BigDecimal("15.00"))
            .createdAt(LocalDateTime.now().minusDays(2))
            .build();

        ReturnResponseMapperImpl mapper = new ReturnResponseMapperImpl();
        ReturnResponse resp = mapper.toResponse(r);

        assertThat(resp.getId()).isEqualTo(r.getId());
        assertThat(resp.getReturnNumber()).isEqualTo(r.getReturnNumber());
        assertThat(resp.getOrderId()).isEqualTo(r.getOrder().getId());
        assertThat(resp.getReason()).isEqualTo(r.getReason());
        assertThat(resp.getCurrentState()).isEqualTo(r.getCurrentState());
        assertThat(resp.getApprovedBy()).isEqualTo(r.getApprovedBy());
        assertThat(resp.getTrackingNumber()).isEqualTo(r.getTrackingNumber());
        assertThat(resp.getRefundAmount()).isEqualTo(r.getRefundAmount());
    }

    @Test
    void mapper_handles_null_order_gracefully() {
        Return r = Return.builder()
            .id(3L)
            .returnNumber("R-NULL")
            .order(null)
            .reason("unknown")
            .currentState(ReturnState.REQUESTED)
            .createdAt(LocalDateTime.now())
            .build();

        ReturnResponseMapperImpl mapper = new ReturnResponseMapperImpl();
        ReturnResponse resp = mapper.toResponse(r);

        assertThat(resp.getOrderId()).isNull();
        assertThat(resp.getReturnNumber()).isEqualTo("R-NULL");
    }
}
