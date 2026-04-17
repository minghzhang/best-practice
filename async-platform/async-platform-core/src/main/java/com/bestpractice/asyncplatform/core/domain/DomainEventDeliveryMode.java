package com.bestpractice.asyncplatform.core.domain;

/**
 * Describes how a domain event should be delivered.
 */
public enum DomainEventDeliveryMode {
    /**
     * Dispatches the event only to in-process subscribers.
     */
    LOCAL,
    /**
     * Dispatches the event only through the async transport.
     */
    ASYNC,
    /**
     * Dispatches the event to local subscribers and also publishes it asynchronously.
     */
    BOTH
}
