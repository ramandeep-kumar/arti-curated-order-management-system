package com.articurated.returns.mapper;

import com.articurated.returns.domain.Return;
import com.articurated.returns.domain.ReturnState;
import com.articurated.returns.dto.ReturnResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReturnResponseMapperImplTest {

    private final ReturnResponseMapperImpl mapper = new ReturnResponseMapperImpl();

    @Test
    void toResponse_delegates_to_static_factory_and_maps_fields() {
        com.articurated.order.domain.Order order = com.articurated.order.domain.Order.builder().id(10L).build();

        Return r = Return.builder()
            .id(3L)
            .returnNumber("R-3")
            .order(order)
            .reason("def")
            .currentState(ReturnState.REQUESTED)
            .approvedBy("mgr")
            .trackingNumber("TRK")
            .refundAmount(new BigDecimal("5.00"))
            .createdAt(LocalDateTime.now().minusDays(2))
            .build();

        ReturnResponse resp = mapper.toResponse(r);

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isEqualTo(r.getId());
        assertThat(resp.getReturnNumber()).isEqualTo(r.getReturnNumber());
    }
}
