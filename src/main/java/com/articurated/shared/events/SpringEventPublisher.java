package com.articurated.shared.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringEventPublisher implements EventPublisher {

    private final ApplicationEventPublisher publisher;

    public SpringEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publishAfterCommit(Object event) {
        try {
            if (org.springframework.transaction.support.TransactionSynchronizationManager.isSynchronizationActive()) {
                org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                    new org.springframework.transaction.support.TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            try {
                                publisher.publishEvent(event);
                            } catch (Exception e) {
                                // swallow to avoid breaking after-commit
                            }
                        }
                    }
                );
            } else {
                publisher.publishEvent(event);
            }
        } catch (Exception e) {
            // log externally if needed
        }
    }

    @Override
    public void publish(Object event) {
        publisher.publishEvent(event);
    }
}
