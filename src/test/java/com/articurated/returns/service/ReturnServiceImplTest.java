package com.articurated.returns.service;

import com.articurated.order.domain.Order;
import com.articurated.order.domain.OrderState;
import com.articurated.order.service.OrderService;
import com.articurated.returns.domain.Return;
import com.articurated.returns.domain.ReturnState;
import com.articurated.returns.dto.CreateReturnRequest;
// ...existing imports...
import com.articurated.returns.service.impl.ReturnServiceImpl;
import com.articurated.shared.events.ProcessRefundEvent;
import com.articurated.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;


import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ReturnServiceImplTest {

    @Mock
    private com.articurated.returns.port.ReturnPersistencePort returnRepository;

    @Mock
    private com.articurated.returns.port.ReturnStateHistoryPersistencePort stateHistoryRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private com.articurated.shared.events.EventPublisher eventPublisher;

    @Mock
    private com.articurated.shared.util.NumberGenerator numberGenerator;

    @InjectMocks
    private ReturnServiceImpl returnService;

    private Order deliveredOrder;

    @BeforeEach
    void setUp() {
        deliveredOrder = Order.builder()
            .id(10L)
            .currentState(OrderState.DELIVERED)
            .subtotal(BigDecimal.valueOf(100))
            .tax(BigDecimal.valueOf(8))
            .shipping(BigDecimal.valueOf(15))
            .total(BigDecimal.valueOf(123))
            .build();
    // ensure createdAt is recent so canBeReturned() returns true
        deliveredOrder.setCreatedAt(java.time.LocalDateTime.now().minusDays(1));
    org.mockito.Mockito.lenient().when(numberGenerator.generate(org.mockito.ArgumentMatchers.anyString())).thenReturn("RET-TEST-1");
    }

    @Test
    void happyPath_createApproveShipReceiveProcessRefund() {
        // create
        CreateReturnRequest req = CreateReturnRequest.builder().orderId(10L).reason("defective").build();
        when(orderService.getOrderById(10L)).thenReturn(deliveredOrder);
        when(returnRepository.save(any(Return.class))).thenAnswer(i -> {
            Return r = i.getArgument(0);
            r.setId(1L);
            return r;
        });

        var created = returnService.createReturn(req);
        assertThat(created).isNotNull();
        verify(stateHistoryRepository).save(any());

        // approve
        when(returnRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(created));
        when(returnRepository.save(any(Return.class))).thenAnswer(i -> i.getArgument(0));
        var approved = returnService.approveReturn(1L, "manager");
        assertThat(approved.getCurrentState()).isEqualTo(ReturnState.APPROVED);
        verify(stateHistoryRepository, atLeastOnce()).save(any());

        // mark in transit
        var inTransit = approved;
        when(returnRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(inTransit));
        inTransit.setCurrentState(ReturnState.APPROVED);
        var transit = returnService.markInTransit(1L, "TRACK123");
        assertThat(transit.getCurrentState()).isEqualTo(ReturnState.IN_TRANSIT);
        verify(stateHistoryRepository, atLeast(2)).save(any());

        // mark received
        when(returnRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(transit));
        transit.setCurrentState(ReturnState.IN_TRANSIT);
        var received = returnService.markReceived(1L);
        assertThat(received.getCurrentState()).isEqualTo(ReturnState.RECEIVED);

        // complete and ensure event published
        when(returnRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(received));
        received.setCurrentState(ReturnState.RECEIVED);
        var completed = returnService.completeReturn(1L);
        assertThat(completed.getCurrentState()).isEqualTo(ReturnState.COMPLETED);
    verify(eventPublisher).publishAfterCommit(any(ProcessRefundEvent.class));
    }

    @Test
    void invalid_markInTransit_whenNotApproved_throwsBusinessException() {
        CreateReturnRequest req = CreateReturnRequest.builder().orderId(10L).reason("defective").build();
        when(orderService.getOrderById(10L)).thenReturn(deliveredOrder);
        when(returnRepository.save(any(Return.class))).thenAnswer(i -> {
            Return r = i.getArgument(0);
            r.setId(2L);
            return r;
        });

        var created = returnService.createReturn(req);
        when(returnRepository.findByIdWithDetails(2L)).thenReturn(Optional.of(created));

        assertThatThrownBy(() -> returnService.markInTransit(2L, "TRACK"))
            .isInstanceOf(BusinessException.class);
    }
}
