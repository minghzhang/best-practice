package com.bestpractice.asyncplatform.runtime.serialization;

import com.bestpractice.asyncplatform.core.domain.DomainEvent;
import com.bestpractice.asyncplatform.core.domain.DomainEventEnvelope;
import com.bestpractice.asyncplatform.core.model.TaskEnvelope;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Default JSON serializer for task envelopes and domain-event envelopes.
 *
 * <p>The runtime first deserializes incoming Kafka records into a generic JsonNode-based
 * envelope, then converts the payload to the handler's declared type just before invocation.</p>
 */
public class JacksonPlatformMessageSerializer implements PlatformMessageSerializer {
    /**
     * Object mapper used for all envelope and event conversions.
     */
    private final ObjectMapper objectMapper;

    /**
     * Creates the default serializer implementation.
     *
     * @param objectMapper mapper used for JSON serialization and conversion
     */
    public JacksonPlatformMessageSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    /**
     * Serializes a task envelope into transport bytes.
     */
    public byte[] serialize(TaskEnvelope<?> envelope) {
        try {
            return objectMapper.writeValueAsBytes(envelope);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize task envelope", ex);
        }
    }

    @Override
    /**
     * Deserializes raw transport bytes into a generic JsonNode-based task envelope.
     */
    public TaskEnvelope<JsonNode> deserialize(byte[] payload) {
        try {
            return objectMapper.readValue(payload, new TypeReference<>() {});
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to deserialize task envelope", ex);
        }
    }

    @Override
    /**
     * Converts a generic JsonNode-based task envelope into the handler's declared payload type.
     */
    public <T> TaskEnvelope<T> convert(TaskEnvelope<JsonNode> envelope, TypeReference<T> typeReference) {
        T convertedPayload = objectMapper.convertValue(envelope.getPayload(), typeReference);
        TaskEnvelope<T> converted = new TaskEnvelope<>();
        // Preserve the original envelope metadata so downstream logic sees the same routing context.
        converted.setTaskId(envelope.getTaskId());
        converted.setTraceId(envelope.getTraceId());
        converted.setName(envelope.getName());
        converted.setTopic(envelope.getTopic());
        converted.setSubTopic(envelope.getSubTopic());
        converted.setPayloadType(envelope.getPayloadType());
        converted.setSchemaVersion(envelope.getSchemaVersion());
        converted.setKey(envelope.getKey());
        converted.setPayload(convertedPayload);
        converted.setTracking(envelope.getTracking());
        converted.setRouting(envelope.getRouting());
        converted.setDelivery(envelope.getDelivery());
        converted.setProducedAt(envelope.getProducedAt());
        return converted;
    }

    @Override
    /**
     * Wraps a typed domain event into a transport-safe envelope.
     */
    public DomainEventEnvelope toDomainEventEnvelope(DomainEvent event) {
        DomainEventEnvelope envelope = new DomainEventEnvelope();
        envelope.setEventId(event.eventId());
        envelope.setEventName(event.eventName());
        envelope.setEventClassName(event.getClass().getName());
        envelope.setEventData(objectMapper.valueToTree(event));
        envelope.setRoutingKey(event.routingKey());
        return envelope;
    }

    @Override
    /**
     * Reconstructs a typed domain event from its transport envelope.
     */
    public <T extends DomainEvent> T convertDomainEvent(DomainEventEnvelope envelope, Class<T> eventType) {
        return objectMapper.convertValue(envelope.getEventData(), eventType);
    }
}
