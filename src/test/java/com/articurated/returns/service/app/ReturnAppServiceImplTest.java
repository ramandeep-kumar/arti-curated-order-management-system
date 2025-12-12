package com.articurated.returns.service.app;

import com.articurated.order.domain.Order;
import com.articurated.order.domain.OrderState;
import com.articurated.order.service.OrderService;
import com.articurated.returns.domain.Return;
import com.articurated.returns.dto.CreateReturnRequest;
import com.articurated.returns.port.ReturnPersistencePort;
import com.articurated.returns.port.ReturnStateHistoryPersistencePort;
import com.articurated.returns.service.domain.DomainReturnService;
import com.articurated.shared.events.EventPublisher;
import com.articurated.shared.util.NumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ReturnAppServiceImplTest {

    @Mock
    private ReturnPersistencePort returnRepository;

    @Mock
    private ReturnStateHistoryPersistencePort stateHistoryRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private DomainReturnService domainReturnService;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private NumberGenerator numberGenerator;

    @InjectMocks
    private ReturnAppServiceImpl returnAppService;

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
        deliveredOrder.setCreatedAt(java.time.LocalDateTime.now().minusDays(1));
        lenient().when(numberGenerator.generate(Mockito.anyString())).thenReturn("RET-TEST-1");
    }

    @Test
    void createReturn_whenRepositorySaveThrows_propagatesException() {
        CreateReturnRequest req = CreateReturnRequest.builder().orderId(10L).reason("defective").build();
        Mockito.when(orderService.getOrderById(10L)).thenReturn(deliveredOrder);
        doThrow(new RuntimeException("db down")).when(returnRepository).save(any(Return.class));

        assertThatThrownBy(() -> returnAppService.createReturn(req))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("db down");
    }
}
