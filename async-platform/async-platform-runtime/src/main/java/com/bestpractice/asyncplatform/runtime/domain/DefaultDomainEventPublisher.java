package com.bestpractice.asyncplatform.runtime.domain;

import com.bestpractice.asyncplatform.core.contract.TaskPublisher;
import com.bestpractice.asyncplatform.core.domain.DomainEvent;
import com.bestpractice.asyncplatform.core.domain.DomainEventDeliveryMode;
import com.bestpractice.asyncplatform.core.domain.DomainEventEnvelope;
import com.bestpractice.asyncplatform.core.domain.DomainEventPublisher;
import com.bestpractice.asyncplatform.core.model.PublishResult;
import com.bestpractice.asyncplatform.core.model.TaskEnvelope;
import com.bestpractice.asyncplatform.runtime.serialization.PlatformMessageSerializer;
import java.util.UUID;

/**
 * Default bridge between domain-event publishing semantics and async task transport.
 *
 * <p>Business code publishes a domain event once. The publisher decides whether it should
 * be dispatched locally, wrapped into an MQ envelope, or both.</p>
 */
public class DefaultDomainEventPublisher implements DomainEventPublisher {
    /**
     * Generic task publisher used for async event transport.
     */
    private final TaskPublisher taskPublisher;
    /**
     * Local subscriber registry used when the event should be dispatched in-process.
     */
    private final DomainEventSubscriberRegistry subscriberRegistry;
    /**
     * Serializer used to wrap domain events into transport envelopes.
     */
    private final PlatformMessageSerializer serializer;
    /**
     * Async topic used for domain-event transport.
     */
    private final String eventTopic;
    /**
     * Sub-topic used to route all async domain-event envelopes.
     */
    private final String eventSubTopic;
    /**
     * Service name written into the event envelope for traceability.
     */
    private final String sourceService;
    /**
     * Flag that determines whether local dispatch is enabled at runtime.
     */
    private final boolean allowLocalDispatch;

    /**
     * Creates the default domain-event publisher.
     *
     * @param taskPublisher task publisher used for async event delivery
     * @param subscriberRegistry registry for local or reconstructed subscriber dispatch
     * @param serializer serializer that wraps domain events into transport envelopes
     * @param eventTopic topic used for async event transport
     * @param eventSubTopic sub-topic used for event routing
     * @param sourceService logical source service name
     * @param allowLocalDispatch whether local dispatch should be performed
     */
    public DefaultDomainEventPublisher(TaskPublisher taskPublisher,
                                       DomainEventSubscriberRegistry subscriberRegistry,
                                       PlatformMessageSerializer serializer,
                                       String eventTopic,
                                       String eventSubTopic,
                                       String sourceService,
                                       boolean allowLocalDispatch) {
        this.taskPublisher = taskPublisher;
        this.subscriberRegistry = subscriberRegistry;
        this.serializer = serializer;
        this.eventTopic = eventTopic;
        this.eventSubTopic = eventSubTopic;
        this.sourceService = sourceService;
        this.allowLocalDispatch = allowLocalDispatch;
    }

    @Override
    /**
     * Publishes the domain event using the requested delivery mode.
     *
     * <p>The event is optionally dispatched locally first and then, unless the mode is
     * {@code LOCAL}, wrapped into a task envelope and sent through the async transport.</p>
     */
    public PublishResult publish(DomainEvent event, DomainEventDeliveryMode deliveryMode) {
        // Local dispatch happens first so in-process subscribers can react immediately.
        if ((deliveryMode == DomainEventDeliveryMode.LOCAL || deliveryMode == DomainEventDeliveryMode.BOTH) && allowLocalDispatch) {
            subscriberRegistry.dispatchLocal(event);
        }
        if (deliveryMode == DomainEventDeliveryMode.LOCAL) {
            return PublishResult.success(event.eventId(), eventTopic);
        }
        // MQ dispatch reuses the task transport so events and tasks share the same runtime path.
        DomainEventEnvelope payload = serializer.toDomainEventEnvelope(event);
        payload.setProducerServiceName(sourceService);
        TaskEnvelope<DomainEventEnvelope> envelope = new TaskEnvelope<>();
        envelope.setTaskId(event.eventId());
        envelope.setTraceId(UUID.randomUUID().toString());
        envelope.setName(event.eventName());
        envelope.setTopic(eventTopic);
        envelope.setSubTopic(eventSubTopic);
        envelope.setPayloadType(DomainEventEnvelope.class.getName());
        envelope.setKey(event.routingKey());
        envelope.setPayload(payload);
        return taskPublisher.publish(envelope);
    }
}
