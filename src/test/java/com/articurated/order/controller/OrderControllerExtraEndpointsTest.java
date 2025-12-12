package com.articurated.order.controller;

import com.articurated.order.domain.Order;
import com.articurated.order.domain.OrderEvent;
import com.articurated.order.dto.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderControllerExtraEndpointsTest {

    private com.articurated.order.mapper.OrderResponseMapper orderResponseMapper;
    private OrderController controller;
    private com.articurated.order.service.app.OrderReadService orderReadService;
    private com.articurated.order.service.app.OrderWriteService orderWriteService;

    @BeforeEach
    void setUp() {
    orderReadService = mock(com.articurated.order.service.app.OrderReadService.class);
    orderWriteService = mock(com.articurated.order.service.app.OrderWriteService.class);
    orderResponseMapper = mock(com.articurated.order.mapper.OrderResponseMapper.class);
    controller = new OrderController(orderReadService, orderWriteService, orderResponseMapper);
    }

    private Order dummyOrder(Long id) {
        Order o = new Order();
        o.setId(id);
        return o;
    }

    @Test
    void payOrder_callsTransitionWithPaymentReceived() {
    Order o = dummyOrder(1L);
    when(orderWriteService.transitionOrderState(1L, OrderEvent.PAYMENT_RECEIVED)).thenReturn(o);
        when(orderResponseMapper.toResponse(o)).thenReturn(
            OrderResponse.builder().id(1L).orderNumber("ORD-1").build());

        ResponseEntity<OrderResponse> orderResp = controller.payOrder(1L);
    assertThat(orderResp.getStatusCode()).isNotNull();
    OrderResponse body = orderResp.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getId()).isEqualTo(1L);
    }

    @Test
    void shipOrder_callsTransitionWithShipEvent() {
    Order o = dummyOrder(2L);
    when(orderWriteService.transitionOrderState(2L, OrderEvent.SHIP_ORDER)).thenReturn(o);
        when(orderResponseMapper.toResponse(o)).thenReturn(
            OrderResponse.builder().id(2L).orderNumber("ORD-2").build());

        ResponseEntity<OrderResponse> orderResp = controller.shipOrder(2L);
    assertThat(orderResp.getStatusCode()).isNotNull();
    OrderResponse body = orderResp.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getId()).isEqualTo(2L);
    }
}
