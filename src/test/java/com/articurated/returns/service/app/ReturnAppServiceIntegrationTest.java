package com.articurated.returns.service.app;

// imports trimmed: Order and OrderState not used in this test
import com.articurated.returns.domain.Return;
import com.articurated.returns.domain.ReturnState;
import com.articurated.returns.handler.EmitRefundOnReturnCompletedHandler;
import com.articurated.returns.handler.RecordReturnStateTransitionHandler;
import com.articurated.returns.port.ReturnPersistencePort;
import com.articurated.returns.port.ReturnStateHistoryPersistencePort;
import com.articurated.returns.service.domain.DomainReturnService;
import com.articurated.shared.events.EventPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {ReturnAppServiceImpl.class, RecordReturnStateTransitionHandler.class, EmitRefundOnReturnCompletedHandler.class, com.articurated.statetransition.StateTransitionHandlerRegistry.class})
@Import(ReturnAppServiceIntegrationTest.TestConfig.class)
@ActiveProfiles("test")
public class ReturnAppServiceIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ReturnPersistencePort returnPersistencePort() {
            return Mockito.mock(ReturnPersistencePort.class);
        }

        @Bean
        public ReturnStateHistoryPersistencePort returnStateHistoryPersistencePort() {
            return Mockito.mock(ReturnStateHistoryPersistencePort.class);
        }

        @Bean
        public DomainReturnService domainReturnService() {
            return Mockito.mock(DomainReturnService.class);
        }

        @Bean
        public com.articurated.order.service.OrderService orderService() {
            return Mockito.mock(com.articurated.order.service.OrderService.class);
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
    private ReturnAppServiceImpl returnAppService;

    @org.springframework.beans.factory.annotation.Autowired
    private ReturnPersistencePort returnRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private ReturnStateHistoryPersistencePort historyRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private DomainReturnService domainReturnService;

    @org.springframework.beans.factory.annotation.Autowired
    private EventPublisher eventPublisher;

    @Test
    void returnComplete_triggersHandlers() {
        // arrange
        Return original = new Return();
        original.setId(200L);
        original.setCurrentState(ReturnState.RECEIVED);

        Return updated = new Return();
        updated.setId(200L);
        updated.setCurrentState(ReturnState.COMPLETED);

        when(domainReturnService.completeReturnDomain(original)).thenReturn(updated);
        when(returnRepository.findByIdWithDetails(200L)).thenReturn(java.util.Optional.of(original));
        when(returnRepository.save(updated)).thenReturn(updated);

    // act
    returnAppService.completeReturn(200L);

        // assert
        verify(returnRepository).save(updated);
        verify(historyRepository, times(1)).save(any());
        verify(eventPublisher, times(1)).publishAfterCommit(any());
    }
}
