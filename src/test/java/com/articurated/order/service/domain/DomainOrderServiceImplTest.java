package com.articurated.order.service.domain;

import com.articurated.order.domain.Order;
import com.articurated.order.domain.OrderEvent;
import com.articurated.order.dto.CreateOrderRequest;
import com.articurated.order.mapper.OrderMapper;
import com.articurated.order.service.OrderAmountCalculator;
import com.articurated.shared.util.NumberGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class DomainOrderServiceImplTest {

    @Test
    void createOrderDomain_buildsOrder() {
    OrderAmountCalculator calc = Mockito.mock(OrderAmountCalculator.class);
    NumberGenerator gen = Mockito.mock(NumberGenerator.class);
    OrderMapper mapper = Mockito.mock(OrderMapper.class);

        when(gen.generate("ORD-")).thenReturn("ORD-1");
        when(calc.calculate(Mockito.anyList())).thenReturn(new com.articurated.order.domain.valueobjects.OrderAmount(new java.math.BigDecimal("10.00"), java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO, new java.math.BigDecimal("10.00")));
        when(mapper.toOrderItems(Mockito.anyList())).thenReturn(java.util.List.of());

        DomainOrderServiceImpl svc = new DomainOrderServiceImpl(calc, gen, mapper);

    CreateOrderRequest req = CreateOrderRequest.builder()
        .customerEmail("x@x.com")
        .firstName("A")
        .lastName("B")
        .address(new com.articurated.order.domain.valueobjects.Address("s","c","st","z","ct"))
        .items(java.util.List.of())
        .build();

        Order o = svc.createOrderDomain(req);
        assertThat(o).isNotNull();
        assertThat(o.getOrderNumber()).isEqualTo("ORD-1");
    }

    @Test
    void processStateTransition_changesState() {
    OrderAmountCalculator calc = Mockito.mock(OrderAmountCalculator.class);
    NumberGenerator gen = Mockito.mock(NumberGenerator.class);
    OrderMapper mapper = Mockito.mock(OrderMapper.class);

        DomainOrderServiceImpl svc = new DomainOrderServiceImpl(calc, gen, mapper);
        Order o = new Order();
        o.setCurrentState(com.articurated.order.domain.OrderState.PENDING_PAYMENT);

        Order updated = svc.processStateTransition(o, OrderEvent.PAYMENT_RECEIVED);
        assertThat(updated.getCurrentState()).isEqualTo(com.articurated.order.domain.OrderState.PAID);
    }
}
