package com.articurated.returns.integration;

import com.articurated.messaging.producer.MessageProducer;
import com.articurated.order.domain.Order;
import com.articurated.order.domain.OrderState;
import com.articurated.order.repository.OrderRepository;
import com.articurated.returns.domain.Return;
import com.articurated.returns.dto.CreateReturnRequest;
import com.articurated.returns.service.ReturnService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@SpringBootTest
public class ReturnIntegrationIT {

    @Autowired
    private ReturnService returnService;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private MessageProducer messageProducer;

    private Order order;

    @BeforeEach
    void setUp() {
        order = Order.builder()
            .orderNumber("ORD-INT-1")
            .customerEmail("a@b.com")
            .customerFirstName("A")
            .customerLastName("B")
            .currentState(OrderState.DELIVERED)
            .subtotal(BigDecimal.valueOf(100))
            .tax(BigDecimal.valueOf(8))
            .shipping(BigDecimal.valueOf(10))
            .total(BigDecimal.valueOf(118))
            .build();
        order = orderRepository.save(order);
    }

    @Test
    void endToEnd_refundEventPublished() throws Exception {
        CreateReturnRequest req = CreateReturnRequest.builder().orderId(order.getId()).reason("defect").build();
        com.articurated.returns.domain.Return r = returnService.createReturn(req);
        r = returnService.approveReturn(r.getId(), "mgr");
        r = returnService.markInTransit(r.getId(), "TRK-1");
        r = returnService.markReceived(r.getId());
        r = returnService.completeReturn(r.getId());

        // ApplicationEventHandler is async; wait briefly for async handling
        Thread.sleep(500);

        verify(messageProducer, atLeastOnce()).sendRefundProcessingMessage(r.getId());
    }
}
