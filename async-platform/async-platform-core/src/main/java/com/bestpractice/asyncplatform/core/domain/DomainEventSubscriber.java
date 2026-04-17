package com.bestpractice.asyncplatform.core.domain;

/**
 * Consumer contract for domain events.
 *
 * <p>Subscribers are matched by logical event name and receive fully typed domain-event
 * objects whether the event originated from local dispatch or asynchronous delivery.</p>
 */
public interface DomainEventSubscriber<T extends DomainEvent> {
    /**
     * Returns the logical event name this subscriber is interested in.
     */
    String eventName();

    /**
     * Returns the Java type of the event payload expected by this subscriber.
     */
    Class<T> eventType();

    /**
     * Handles one domain event instance.
     */
    void onEvent(T event);
}
