package com.articurated.shared.events;

/**
 * Maps domain integration events to outbound messaging actions.
 * Implementations should be Spring components so the handler can discover them.
 */
public interface EventMapper {
    boolean supports(Object event);
    void mapAndSend(Object event);
}
