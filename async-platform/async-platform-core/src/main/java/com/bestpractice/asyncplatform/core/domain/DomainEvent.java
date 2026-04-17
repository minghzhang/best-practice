package com.bestpractice.asyncplatform.core.domain;

/**
 * Minimal contract for business domain events.
 *
 * <p>Domain events describe facts that happened in the business domain. The platform
 * can dispatch them locally, asynchronously, or both, without the event itself knowing
 * which transport is used.</p>
 */
public interface DomainEvent {
    /**
     * Returns the unique identifier of this event instance.
     */
    String eventId();

    /**
     * Returns the logical event name used for subscriber matching.
     */
    String eventName();

    /**
     * Returns the routing key used when the event is transported asynchronously.
     */
    String routingKey();
}
