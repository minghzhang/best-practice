package com.bestpractice.asyncplatform.starter;

import com.bestpractice.asyncplatform.core.contract.SingleMqHandler;
import com.bestpractice.asyncplatform.core.domain.DomainEventEnvelope;
import com.bestpractice.asyncplatform.core.model.ConsumeResult;
import com.bestpractice.asyncplatform.core.model.TaskEnvelope;
import com.bestpractice.asyncplatform.runtime.domain.DomainEventSubscriberRegistry;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Set;

/**
 * Internal bridge handler that turns MQ-delivered domain-event envelopes back into local subscriber calls.
 */
public class DomainEventMqHandler implements SingleMqHandler<DomainEventEnvelope> {
    /**
     * Registry that dispatches reconstructed events to application subscribers.
     */
    private final DomainEventSubscriberRegistry subscriberRegistry;
    /**
     * Sub-topic reserved for transported domain events.
     */
    private final String subTopic;

    /**
     * Creates the bridge handler used for async domain-event delivery.
     */
    public DomainEventMqHandler(DomainEventSubscriberRegistry subscriberRegistry, String subTopic) {
        this.subscriberRegistry = subscriberRegistry;
        this.subTopic = subTopic;
    }

    @Override
    /**
     * Returns the stable handler name referenced by starter bindings.
     */
    public String handlerName() {
        return "domainEventMqHandler";
    }

    @Override
    /**
     * Declares that this handler expects a domain-event envelope payload.
     */
    public TypeReference<DomainEventEnvelope> payloadType() {
        return new TypeReference<>() {};
    }

    @Override
    /**
     * Restricts the handler to the configured domain-event sub-topic.
     */
    public Set<String> supportedSubTopics() {
        return Set.of(subTopic);
    }

    @Override
    /**
     * Replays the transported event into the local subscriber registry.
     */
    public ConsumeResult handle(TaskEnvelope<DomainEventEnvelope> envelope) {
        subscriberRegistry.dispatchAsyncEnvelope(envelope.getPayload());
        return ConsumeResult.success();
    }
}
