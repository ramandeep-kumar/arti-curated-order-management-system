package com.articurated.order.mapper;

import com.articurated.order.domain.Order;
import com.articurated.order.domain.OrderState;
import com.articurated.order.dto.OrderResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OrderResponseMapperImplTest {

    private final OrderResponseMapperImpl mapper = new OrderResponseMapperImpl();

    @Test
    void toResponse_delegates_to_static_factory_and_maps_fields() {
        Order order = Order.builder()
            .id(7L)
            .orderNumber("ORD-7")
            .customerFirstName("A")
            .customerLastName("B")
            .customerEmail("a@b.com")
            .subtotal(new BigDecimal("1.00"))
            .tax(new BigDecimal("0.10"))
            .shipping(new BigDecimal("0.50"))
            .total(new BigDecimal("1.60"))
            .currentState(OrderState.PENDING_PAYMENT)
            .createdAt(LocalDateTime.now().minusDays(1))
            .updatedAt(LocalDateTime.now())
            .build();

        OrderResponse resp = mapper.toResponse(order);

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isEqualTo(order.getId());
        assertThat(resp.getOrderNumber()).isEqualTo(order.getOrderNumber());
        assertThat(resp.getCustomerEmail()).isEqualTo(order.getCustomerEmail());
    }
}
