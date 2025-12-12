package com.articurated.order.service.app;

import com.articurated.order.domain.Order;
import com.articurated.order.domain.OrderEvent;
import com.articurated.order.domain.OrderState;
import com.articurated.order.handler.EmitInvoiceOnPaidHandler;
import com.articurated.order.handler.RecordOrderStateTransitionHandler;
import com.articurated.order.port.OrderPersistencePort;
import com.articurated.order.port.OrderStateHistoryPersistencePort;
import com.articurated.order.service.domain.DomainOrderService;
import com.articurated.order.statemachine.OrderStateMachineManager;
import com.articurated.shared.events.EventPublisher;
import com.articurated.statetransition.StateTransitionHandlerRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {OrderAppServiceImpl.class, RecordOrderStateTransitionHandler.class, EmitInvoiceOnPaidHandler.class, StateTransitionHandlerRegistry.class})
@Import(OrderAppServiceIntegrationIT.TestConfig.class)
@ActiveProfiles("test")
public class OrderAppServiceIntegrationIT {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public OrderPersistencePort orderPersistencePort() {
            return Mockito.mock(OrderPersistencePort.class);
        }

        @Bean
        public OrderStateHistoryPersistencePort orderStateHistoryPersistencePort() {
            return Mockito.mock(OrderStateHistoryPersistencePort.class);
        }

        @Bean
        public com.articurated.returns.service.domain.DomainReturnService domainReturnService() {
            return Mockito.mock(com.articurated.returns.service.domain.DomainReturnService.class);
        }

        @Bean
        public OrderStateMachineManager orderStateMachineManager() {
            return Mockito.mock(OrderStateMachineManager.class);
        }

        @Bean
        public EventPublisher eventPublisher() {
            return Mockito.mock(EventPublisher.class);
        }

        @Bean
        public com.articurated.shared.util.NumberGenerator numberGenerator() {
            return new com.articurated.shared.util.TimeRandomNumberGenerator();
        }
    }

    @org.springframework.beans.factory.annotation.Autowired
    private OrderAppServiceImpl orderAppService;

    @org.springframework.beans.factory.annotation.Autowired
    private OrderPersistencePort orderRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private OrderStateHistoryPersistencePort historyRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private DomainOrderService domainOrderService;

    @org.springframework.beans.factory.annotation.Autowired
    private EventPublisher eventPublisher;

    @Test
    void transitionOrderState_triggersHandlers() {
        // arrange
        Order original = new Order();
        original.setId(100L);
        original.setCurrentState(OrderState.PENDING_PAYMENT);

        Order updated = new Order();
        updated.setId(100L);
        updated.setCurrentState(OrderState.PAID);

        when(domainOrderService.processStateTransition(original, OrderEvent.PAYMENT_RECEIVED)).thenReturn(updated);
        when(orderRepository.findById(100L)).thenReturn(java.util.Optional.of(original));
        when(orderRepository.save(updated)).thenReturn(updated);

        // act
    orderAppService.transitionOrderState(100L, OrderEvent.PAYMENT_RECEIVED);

        // assert
        verify(orderRepository).save(updated);
        // handler should record state history via the history repository
        verify(historyRepository, times(1)).save(any());
        // handler should emit invoice generation event
        verify(eventPublisher, times(1)).publishAfterCommit(any());
    }
}
