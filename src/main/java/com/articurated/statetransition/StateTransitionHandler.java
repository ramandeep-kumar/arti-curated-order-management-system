package com.articurated.statetransition;

public interface StateTransitionHandler<T, S> {
    /**
     * Handle a state transition for an entity.
     * @param entity the entity instance
     * @param from previous state (may be null)
     * @param to new state
     * @param reason reason for transition
     */
    void handle(T entity, S from, S to, String reason);
}
