package com.articurated.order.mapper;

import com.articurated.order.dto.OrderItemRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperImplTest {

    private final OrderMapperImpl mapper = new OrderMapperImpl();

    @Test
    void toOrderItems_mapsFieldsCorrectly() {
        OrderItemRequest req = OrderItemRequest.builder()
            .productName("Widget")
            .price(new BigDecimal("10.00"))
            .quantity(2)
            .build();

        var items = mapper.toOrderItems(List.of(req));
        assertThat(items).hasSize(1);
        var item = items.get(0);
        assertThat(item.getProductName()).isEqualTo("Widget");
        assertThat(item.getPrice()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(item.getQuantity()).isEqualTo(2);
        assertThat(item.getTotal()).isEqualByComparingTo(new BigDecimal("20.00"));
    }
}
