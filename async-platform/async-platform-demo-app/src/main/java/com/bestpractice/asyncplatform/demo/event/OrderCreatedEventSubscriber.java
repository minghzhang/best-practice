package com.bestpractice.asyncplatform.demo.event;

import com.bestpractice.asyncplatform.core.domain.DomainEventSubscriber;
import com.bestpractice.asyncplatform.demo.observation.InMemoryDemoObservationStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Demo subscriber that records observed order-created domain events.
 */
@Slf4j
@Component
public class OrderCreatedEventSubscriber implements DomainEventSubscriber<OrderCreatedEvent> {
    /**
     * Shared observation store used by the demo and tests.
     */
    private final InMemoryDemoObservationStore observationStore;

    /**
     * Creates the demo subscriber.
     */
    public OrderCreatedEventSubscriber(InMemoryDemoObservationStore observationStore) {
        this.observationStore = observationStore;
    }

    @Override
    /**
     * Returns the event name consumed by this subscriber.
     */
    public String eventName() {
        return "order.created.domain-event";
    }

    @Override
    /**
     * Returns the typed event class expected by this subscriber.
     */
    public Class<OrderCreatedEvent> eventType() {
        return OrderCreatedEvent.class;
    }

    @Override
    /**
     * Records the observed event in the demo observation store.
     */
    public void onEvent(OrderCreatedEvent event) {
        log.info("Observed domain event. eventId={}, orderId={}, source={}",
                event.eventId(), event.orderId(), event.source());
        observationStore.recordDomainEvent(event.orderId());
    }
}
