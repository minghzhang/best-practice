package com.bestpractice.asyncplatform.demo.event;

import com.bestpractice.asyncplatform.core.domain.DomainEvent;
import java.time.Instant;

/**
 * Demo domain event representing successful order creation.
 *
 * @param eventId unique event identifier
 * @param orderId business order identifier
 * @param createdAt event creation timestamp
 * @param source logical publisher of the event
 */
public record OrderCreatedEvent(String eventId, String orderId, Instant createdAt, String source) implements DomainEvent {
    @Override
    /**
     * Returns the logical event name used by subscribers.
     */
    public String eventName() {
        return "order.created.domain-event";
    }

    @Override
    /**
     * Uses the order identifier as the async routing key.
     */
    public String routingKey() {
        return orderId;
    }
}
