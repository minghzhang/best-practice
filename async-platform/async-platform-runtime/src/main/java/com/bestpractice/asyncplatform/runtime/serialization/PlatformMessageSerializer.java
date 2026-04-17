package com.bestpractice.asyncplatform.runtime.serialization;

import com.bestpractice.asyncplatform.core.domain.DomainEvent;
import com.bestpractice.asyncplatform.core.domain.DomainEventEnvelope;
import com.bestpractice.asyncplatform.core.model.TaskEnvelope;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Serializer abstraction used by the runtime and transport adapter.
 */
public interface PlatformMessageSerializer {
    /**
     * Serializes a task envelope into raw transport bytes.
     */
    byte[] serialize(TaskEnvelope<?> envelope);

    /**
     * Deserializes raw transport bytes into a generic JsonNode-based task envelope.
     */
    TaskEnvelope<JsonNode> deserialize(byte[] payload);

    /**
     * Converts a generic envelope to the handler's declared payload type.
     */
    <T> TaskEnvelope<T> convert(TaskEnvelope<JsonNode> envelope, TypeReference<T> typeReference);

    /**
     * Wraps a typed domain event into a transport-safe envelope.
     */
    DomainEventEnvelope toDomainEventEnvelope(DomainEvent event);

    /**
     * Restores a typed domain event from its transport envelope.
     */
    <T extends DomainEvent> T convertDomainEvent(DomainEventEnvelope envelope, Class<T> eventType);
}
