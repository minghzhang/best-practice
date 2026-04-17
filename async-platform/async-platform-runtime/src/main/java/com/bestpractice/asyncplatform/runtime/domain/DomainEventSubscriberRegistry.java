package com.bestpractice.asyncplatform.runtime.domain;

import com.bestpractice.asyncplatform.core.domain.DomainEvent;
import com.bestpractice.asyncplatform.core.domain.DomainEventEnvelope;

/**
 * Internal registry contract for dispatching domain events to subscribers.
 */
public interface DomainEventSubscriberRegistry {
    /**
     * Dispatches a typed in-memory domain event to matching local subscribers.
     */
    void dispatchLocal(DomainEvent event);

    /**
     * Dispatches an asynchronously transported event envelope to matching subscribers.
     */
    void dispatchAsyncEnvelope(DomainEventEnvelope envelope);
}
