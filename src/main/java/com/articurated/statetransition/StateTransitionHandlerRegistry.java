package com.articurated.statetransition;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StateTransitionHandlerRegistry {

    private final List<StateTransitionHandler<?,?>> handlers;

    public StateTransitionHandlerRegistry(List<StateTransitionHandler<?,?>> handlers) {
        this.handlers = handlers;
    }

    public List<StateTransitionHandler<?,?>> getHandlers() {
        return handlers;
    }
}
