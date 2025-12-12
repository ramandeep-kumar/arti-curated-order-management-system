package com.articurated.order.service.app;

import com.articurated.order.domain.Order;
import com.articurated.order.domain.OrderItem;
import com.articurated.order.domain.OrderState;
import com.articurated.order.dto.OrderItemRequest;
import com.articurated.order.mapper.OrderMapper;
import com.articurated.order.port.OrderPersistencePort;
import com.articurated.order.service.OrderAmountCalculator;
import com.articurated.statetransition.StateTransitionHandlerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderAppServiceImplTest {

    private OrderPersistencePort orderRepo;
    private StateTransitionHandlerRegistry registry;
    private com.articurated.order.statemachine.OrderStateMachineManager sm;
    private com.articurated.order.service.domain.DomainOrderService domainService;
    private OrderMapper orderMapper;
    private OrderAmountCalculator amountCalculator;

    private com.articurated.order.service.app.OrderAppServiceImpl appService;

    @BeforeEach
    void setup() {
        orderRepo = mock(OrderPersistencePort.class);
        registry = mock(StateTransitionHandlerRegistry.class);
        sm = mock(com.articurated.order.statemachine.OrderStateMachineManager.class);
        domainService = mock(com.articurated.order.service.domain.DomainOrderService.class);
        orderMapper = mock(OrderMapper.class);
        amountCalculator = mock(OrderAmountCalculator.class);

        appService = new com.articurated.order.service.app.OrderAppServiceImpl(
                orderRepo,
                mock(com.articurated.order.port.OrderStateHistoryPersistencePort.class),
                domainService,
                sm,
                registry,
                orderMapper,
                amountCalculator
        );
    }

    @Test
    void testAddItemsAllowedState_shouldAppendAndRecalculate() {
        Order existing = Order.builder().id(1L).currentState(OrderState.PENDING_PAYMENT).build();
        existing.setItems(new java.util.ArrayList<>());

        when(orderRepo.findByIdWithAllDetails(1L)).thenReturn(Optional.of(existing));

        OrderItemRequest req = OrderItemRequest.builder()
                .productName("Extra")
                .price(BigDecimal.valueOf(10))
                .quantity(2)
                .build();

        OrderItem mapped = OrderItem.builder().productName("Extra").price(BigDecimal.valueOf(10)).quantity(2).total(BigDecimal.valueOf(20)).build();
        when(orderMapper.toOrderItems(List.of(req))).thenReturn(List.of(mapped));

        when(amountCalculator.calculate(existing.getItems())).thenReturn(new com.articurated.order.domain.valueobjects.OrderAmount(BigDecimal.valueOf(20), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(20)));

        when(orderRepo.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order updated = appService.addItems(1L, List.of(req));

        assertNotNull(updated);
        assertEquals(1, updated.getItems().size());
        assertEquals(BigDecimal.valueOf(20), updated.getTotal());
        verify(orderRepo).save(any(Order.class));
    }

    @Test
    void testAddItemsForbiddenState_shouldThrowBusinessException() {
        Order existing = Order.builder().id(2L).currentState(OrderState.SHIPPED).build();
        existing.setItems(new java.util.ArrayList<>());
        when(orderRepo.findByIdWithAllDetails(2L)).thenReturn(Optional.of(existing));

        OrderItemRequest req = OrderItemRequest.builder()
                .productName("Extra")
                .price(BigDecimal.valueOf(10))
                .quantity(1)
                .build();

        com.articurated.shared.exception.BusinessException ex = assertThrows(com.articurated.shared.exception.BusinessException.class,
                () -> appService.addItems(2L, List.of(req)));

        assertTrue(ex.getMessage().contains("Cannot add items"));
        verify(orderRepo, never()).save(any());
    }
}
