package com.articurated.order.mapper;

import com.articurated.order.domain.Order;
import com.articurated.order.domain.OrderItem;
import com.articurated.order.domain.OrderState;
import com.articurated.order.dto.OrderResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderResponseFromTest {

    @Test
    void mapper_maps_order_to_response() {
        OrderItem item = OrderItem.builder()
            .id(11L)
            .productName("Widget")
            .price(new BigDecimal("10.00"))
            .quantity(2)
            .total(new BigDecimal("20.00"))
            .build();

        Order order = Order.builder()
            .id(1L)
            .orderNumber("ORD-123")
            .customerFirstName("Jane")
            .customerLastName("Doe")
            .customerEmail("jane@example.com")
            .subtotal(new BigDecimal("20.00"))
            .tax(new BigDecimal("2.00"))
            .shipping(new BigDecimal("5.00"))
            .total(new BigDecimal("27.00"))
            .currentState(OrderState.DELIVERED)
            .street("1 Main St")
            .city("City")
            .state("ST")
            .zipCode("12345")
            .country("Country")
            .createdAt(LocalDateTime.now().minusDays(1))
            .updatedAt(LocalDateTime.now())
            .items(List.of(item))
            .build();

        item.setOrder(order);

        OrderResponseMapperImpl mapper = new OrderResponseMapperImpl();
        OrderResponse resp = mapper.toResponse(order);

        assertThat(resp.getId()).isEqualTo(order.getId());
        assertThat(resp.getOrderNumber()).isEqualTo(order.getOrderNumber());
        assertThat(resp.getCustomerEmail()).isEqualTo(order.getCustomerEmail());
        assertThat(resp.getCustomerName()).contains("Jane");
        assertThat(resp.getCurrentState()).isEqualTo(order.getCurrentState());
        assertThat(resp.getAmount()).isNotNull();
        assertThat(resp.getItems()).hasSize(1);
        assertThat(resp.getAddress()).isNotNull();
    }
}
