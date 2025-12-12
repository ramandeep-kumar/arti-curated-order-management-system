package com.articurated.order.mapper;

import com.articurated.order.domain.OrderItem;
import com.articurated.order.dto.OrderItemResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemResponseTest {

    @Test
    void from_maps_item_fields() {
        OrderItem it = OrderItem.builder()
            .id(7L)
            .productName("Gadget")
            .price(new BigDecimal("5.50"))
            .quantity(3)
            .total(new BigDecimal("16.50"))
            .build();

        OrderItemResponse resp = OrderItemResponse.from(it);

        assertThat(resp.getId()).isEqualTo(it.getId());
        assertThat(resp.getProductName()).isEqualTo(it.getProductName());
        assertThat(resp.getPrice()).isEqualTo(it.getPrice());
        assertThat(resp.getQuantity()).isEqualTo(it.getQuantity());
        assertThat(resp.getTotal()).isEqualTo(it.getTotal());
    }

    @Test
    void from_handles_null_fields() {
        OrderItem it = OrderItem.builder()
            .id(8L)
            .productName(null)
            .price(null)
            .quantity(null)
            .total(null)
            .build();

        OrderItemResponse resp = OrderItemResponse.from(it);

        assertThat(resp.getId()).isEqualTo(it.getId());
        assertThat(resp.getProductName()).isNull();
        assertThat(resp.getPrice()).isNull();
        assertThat(resp.getQuantity()).isNull();
        assertThat(resp.getTotal()).isNull();
    }

    @Test
    void from_handles_zero_quantity() {
        OrderItem it = OrderItem.builder()
            .id(9L)
            .productName("Freebie")
            .price(new java.math.BigDecimal("0.00"))
            .quantity(0)
            .total(new java.math.BigDecimal("0.00"))
            .build();

        OrderItemResponse resp = OrderItemResponse.from(it);

        assertThat(resp.getQuantity()).isEqualTo(0);
        assertThat(resp.getTotal()).isEqualTo(new java.math.BigDecimal("0.00"));
    }
}
