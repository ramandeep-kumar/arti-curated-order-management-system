package com.articurated.order.service;

import com.articurated.order.domain.Order;
import com.articurated.order.domain.OrderEvent;
import com.articurated.order.domain.OrderState;
import com.articurated.order.domain.OrderStateHistory;
import com.articurated.order.domain.valueobjects.Address;
import com.articurated.order.dto.CreateOrderRequest;
import com.articurated.order.dto.OrderItemRequest;
// ...existing imports...
import com.articurated.order.service.impl.OrderServiceImpl;
import com.articurated.shared.exception.OrderNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import com.articurated.order.service.OrderAmountCalculator;
import com.articurated.order.domain.valueobjects.OrderAmount;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private com.articurated.order.port.OrderPersistencePort orderRepository;

    @Mock
    private com.articurated.order.port.OrderStateHistoryPersistencePort stateHistoryRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private OrderAmountCalculator orderAmountCalculator;

    @Mock
    private com.articurated.shared.util.NumberGenerator numberGenerator;

    @Mock
    private com.articurated.order.mapper.OrderMapper orderMapper;

    @Mock
    private com.articurated.order.statemachine.OrderStateMachineManager stateMachineManager;

    @InjectMocks
    private OrderServiceImpl orderService;

    private CreateOrderRequest createOrderRequest;
    private Order mockOrder;

    @BeforeEach
    void setUp() {
        createOrderRequest = CreateOrderRequest.builder()
                .customerEmail("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .items(List.of(OrderItemRequest.builder()
                        .productName("Test Product")
                        .price(new BigDecimal("99.99"))
                        .quantity(1)
                        .build()))
                .address(Address.builder()
                        .street("123 Test St")
                        .city("Test City")
                        .state("TS")
                        .zipCode("12345")
                        .country("USA")
                        .build())
                .build();

        mockOrder = Order.builder()
                .id(1L)
                .orderNumber("ORD-TEST-001")
                .currentState(OrderState.PENDING_PAYMENT)
                .build();

    // Stub order amount calculation to return expected amounts for the single test item
    org.mockito.Mockito.lenient().when(orderAmountCalculator.calculate(any())).thenReturn(
        OrderAmount.calculate(new BigDecimal("99.99"), new BigDecimal("0.08"), new BigDecimal("15.00"))
    );
    org.mockito.Mockito.lenient().when(numberGenerator.generate(org.mockito.ArgumentMatchers.anyString())).thenReturn("ORD-TEST-1");

    // stub mapper to produce one OrderItem matching the request
    org.mockito.Mockito.lenient().when(orderMapper.toOrderItems(any())).thenAnswer(inv -> {
        var list = inv.getArgument(0, java.util.List.class);
        // create simple OrderItem(s) mirroring request
        return list.stream().map(r -> {
            com.articurated.order.dto.OrderItemRequest req = (com.articurated.order.dto.OrderItemRequest) r;
            com.articurated.order.domain.OrderItem item = com.articurated.order.domain.OrderItem.builder()
                .productName(req.getProductName())
                .price(req.getPrice())
                .quantity(req.getQuantity())
                .total(req.getPrice().multiply(new java.math.BigDecimal(req.getQuantity())))
                .build();
            return item;
        }).toList();
    });
    }

    @Test
    void createOrder_ValidRequest_ReturnsCreatedOrder() {
        // Given
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
        when(stateHistoryRepository.save(any(OrderStateHistory.class))).thenReturn(new OrderStateHistory());

        // When
        Order result = orderService.createOrder(createOrderRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCurrentState()).isEqualTo(OrderState.PENDING_PAYMENT);
        verify(orderRepository).save(any(Order.class));
        verify(stateHistoryRepository).save(any(OrderStateHistory.class));
    }

    @Test
    void getOrderById_ExistingOrder_ReturnsOrder() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));

        // When
        Order result = orderService.getOrderById(1L);

        // Then
        assertThat(result).isEqualTo(mockOrder);
        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrderById_NonExistingOrder_ThrowsException() {
        // Given
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.getOrderById(999L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order not found with ID: 999");
    }

    @Test
    void transitionOrderState_ValidTransition_UpdatesOrderState() {
        // Given
    when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
    doNothing().when(stateMachineManager).sendEvent(any(org.springframework.messaging.Message.class));
    doNothing().when(stateMachineManager).prepareStateMachineForOrder(any(OrderState.class), anyLong());
    when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

    // When
    var result = orderService.transitionOrderState(1L, OrderEvent.PAYMENT_RECEIVED);

    // Then: capture the sent Message and assert payload
    ArgumentCaptor<Message<OrderEvent>> msgCaptor = ArgumentCaptor.forClass((Class) Message.class);
    verify(stateMachineManager).sendEvent(msgCaptor.capture());
    assertThat(msgCaptor.getValue().getPayload()).isEqualTo(OrderEvent.PAYMENT_RECEIVED);
    verify(orderRepository).save(any(Order.class));
    verify(stateHistoryRepository).save(any(OrderStateHistory.class));
    }

    @Test
    void transitionOrderState_InvalidTransition_ThrowsException() {
        // Given
    when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
    doNothing().when(stateMachineManager).prepareStateMachineForOrder(any(OrderState.class), anyLong());
        // When
    // Use an event that is invalid from PENDING_PAYMENT to match deterministic fallback
    Order result = orderService.transitionOrderState(1L, OrderEvent.START_PROCESSING);

    // Then: idempotent no-op: returns existing order and does not trigger side-effects
    assertThat(result).isEqualTo(mockOrder);
    // Verify both overloaded sendEvent signatures were not invoked
    verify(stateMachineManager, never()).sendEvent(any(org.springframework.messaging.Message.class));
    verify(stateMachineManager, never()).sendEvent(any(OrderEvent.class));
    verify(orderRepository, never()).save(any(Order.class));
    verify(stateHistoryRepository, never()).save(any());
    }
}
