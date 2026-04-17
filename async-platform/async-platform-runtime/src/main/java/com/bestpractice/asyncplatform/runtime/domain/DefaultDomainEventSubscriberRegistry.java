package com.bestpractice.asyncplatform.runtime.domain;

import com.bestpractice.asyncplatform.core.domain.DomainEvent;
import com.bestpractice.asyncplatform.core.domain.DomainEventEnvelope;
import com.bestpractice.asyncplatform.core.domain.DomainEventSubscriber;
import com.bestpractice.asyncplatform.runtime.serialization.PlatformMessageSerializer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Registry for local and async domain-event subscribers.
 *
 * <p>Local dispatch can invoke subscribers directly because the event is already typed.
 * Async dispatch first reconstructs the event object from the transport envelope.</p>
 */
public class DefaultDomainEventSubscriberRegistry implements DomainEventSubscriberRegistry {
    /**
     * Subscriber index grouped by logical event name.
     */
    private final Map<String, List<DomainEventSubscriber<?>>> subscribersByEventName;
    /**
     * Serializer used to rebuild typed domain events from async envelopes.
     */
    private final PlatformMessageSerializer serializer;

    /**
     * Creates the subscriber registry from discovered subscriber beans.
     *
     * @param subscribers subscribers available in the application context
     * @param serializer serializer used to reconstruct async event payloads
     */
    public DefaultDomainEventSubscriberRegistry(Collection<DomainEventSubscriber<?>> subscribers,
                                                PlatformMessageSerializer serializer) {
        this.subscribersByEventName = subscribers.stream()
                .collect(Collectors.groupingBy(DomainEventSubscriber::eventName));
        this.serializer = serializer;
    }

    @Override
    /**
     * Dispatches a typed domain event directly to all matching in-process subscribers.
     */
    public void dispatchLocal(DomainEvent event) {
        for (DomainEventSubscriber<?> subscriber : subscribersByEventName.getOrDefault(event.eventName(), List.of())) {
            invokeLocal(subscriber, event);
        }
    }

    @Override
    /**
     * Dispatches an asynchronously transported event envelope to all matching subscribers.
     */
    public void dispatchAsyncEnvelope(DomainEventEnvelope envelope) {
        for (DomainEventSubscriber<?> subscriber : subscribersByEventName.getOrDefault(envelope.getEventName(), List.of())) {
            invokeAsync(subscriber, envelope);
        }
    }

    /**
     * Invokes a subscriber when the event is already available as a typed in-memory object.
     */
    private <T extends DomainEvent> void invokeLocal(DomainEventSubscriber<?> subscriber, DomainEvent event) {
        DomainEventSubscriber<T> typed = cast(subscriber);
        typed.onEvent(typed.eventType().cast(event));
    }

    /**
     * Reconstructs a typed event from the transport envelope and invokes the subscriber.
     */
    private <T extends DomainEvent> void invokeAsync(DomainEventSubscriber<?> subscriber, DomainEventEnvelope envelope) {
        DomainEventSubscriber<T> typed = cast(subscriber);
        // MQ delivery only carries the generic envelope, so the original event type is restored here.
        T event = serializer.convertDomainEvent(envelope, typed.eventType());
        typed.onEvent(event);
    }

    @SuppressWarnings("unchecked")
    /**
     * Casts the generic subscriber reference to the concrete event type expected at invocation time.
     */
    private <T extends DomainEvent> DomainEventSubscriber<T> cast(DomainEventSubscriber<?> subscriber) {
        return (DomainEventSubscriber<T>) subscriber;
    }
}
