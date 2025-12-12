package com.articurated.shared.events;

public interface EventPublisher {
    void publishAfterCommit(Object event);
    void publish(Object event);
}
