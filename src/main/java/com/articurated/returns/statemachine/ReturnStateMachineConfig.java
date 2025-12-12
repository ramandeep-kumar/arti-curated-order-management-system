package com.articurated.returns.statemachine;

import com.articurated.returns.domain.ReturnEvent;
import com.articurated.returns.domain.ReturnState;
import com.articurated.shared.events.ProcessRefundEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;

import java.util.EnumSet;

@Configuration
@EnableStateMachine(name = "returnStateMachine")
@RequiredArgsConstructor
public class ReturnStateMachineConfig {
    
    private final ApplicationEventPublisher eventPublisher;
    
    @Bean
    public StateMachineBuilder.Builder<ReturnState, ReturnEvent> returnStateMachineBuilder() throws Exception {
        StateMachineBuilder.Builder<ReturnState, ReturnEvent> builder = StateMachineBuilder.builder();
        
        builder.configureStates()
            .withStates()
            .initial(ReturnState.REQUESTED)
            .states(EnumSet.allOf(ReturnState.class));
        
        builder.configureTransitions()
            .withExternal()
                .source(ReturnState.REQUESTED).target(ReturnState.APPROVED)
                .event(ReturnEvent.APPROVE)
            .and()
            .withExternal()
                .source(ReturnState.REQUESTED).target(ReturnState.REJECTED)
                .event(ReturnEvent.REJECT)
            .and()
            .withExternal()
                .source(ReturnState.APPROVED).target(ReturnState.IN_TRANSIT)
                .event(ReturnEvent.SHIP_BACK)
            .and()
            .withExternal()
                .source(ReturnState.IN_TRANSIT).target(ReturnState.RECEIVED)
                .event(ReturnEvent.RECEIVE_ITEM)
            .and()
            .withExternal()
                .source(ReturnState.RECEIVED).target(ReturnState.COMPLETED)
                .event(ReturnEvent.PROCESS_REFUND)
                .action(processRefundAction());
        
        return builder;
    }
    
    @Bean
    public org.springframework.statemachine.StateMachine<ReturnState, ReturnEvent> returnStateMachine() throws Exception {
        return returnStateMachineBuilder().build();
    }
    
    private Action<ReturnState, ReturnEvent> processRefundAction() {
        return context -> {
            Long returnId = (Long) context.getExtendedState().getVariables().get("returnId");
            if (returnId != null) {
                eventPublisher.publishEvent(new ProcessRefundEvent(returnId));
            }
        };
    }
}
