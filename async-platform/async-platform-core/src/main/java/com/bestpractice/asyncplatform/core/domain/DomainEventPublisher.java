package com.bestpractice.asyncplatform.core.domain;

import com.bestpractice.asyncplatform.core.model.PublishResult;

/**
 * High-level entry point for publishing domain events.
 *
 * <p>The platform keeps domain-event publishing separate from generic task publishing so
 * application code can express business intent directly.</p>
 */
public interface DomainEventPublisher {
    /**
     * Publishes one domain event using the requested delivery mode.
     *
     * @param event the domain event to publish
     * @param deliveryMode whether the event should be delivered locally, asynchronously, or both
     * @return a publish result summarizing the async transport outcome
     */
    PublishResult publish(DomainEvent event, DomainEventDeliveryMode deliveryMode);
}
