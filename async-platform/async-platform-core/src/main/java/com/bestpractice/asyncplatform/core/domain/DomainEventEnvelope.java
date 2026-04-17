package com.bestpractice.asyncplatform.core.domain;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Transport-safe representation of a domain event.
 *
 * <p>Domain events are converted into this envelope before they are put on the wire.
 * The envelope keeps the event identity and metadata while storing the actual event body
 * as JSON so subscribers can reconstruct the original event type later.</p>
 */
public class DomainEventEnvelope {
    /**
     * Unique identifier of the event instance.
     */
    private String eventId;
    /**
     * Logical event name used to select subscribers.
     */
    private String eventName;
    /**
     * Fully qualified Java class name of the original event.
     */
    private String eventClassName;
    /**
     * Serialized event body.
     */
    private JsonNode eventData;
    /**
     * Service that originally published the event.
     */
    private String producerServiceName;
    /**
     * Flag reserved for future cross-group consumption policies.
     */
    private boolean allowOtherGroupConsume;
    /**
     * Routing key used for partition selection during async delivery.
     */
    private String routingKey;

    /**
     * Returns the unique identifier of the event instance.
     */
    public String getEventId() { return eventId; }
    /**
     * Updates the unique identifier of the event instance.
     */
    public void setEventId(String eventId) { this.eventId = eventId; }
    /**
     * Returns the logical event name used for subscriber matching.
     */
    public String getEventName() { return eventName; }
    /**
     * Updates the logical event name used for subscriber matching.
     */
    public void setEventName(String eventName) { this.eventName = eventName; }
    /**
     * Returns the original Java class name of the domain event.
     */
    public String getEventClassName() { return eventClassName; }
    /**
     * Updates the original Java class name of the domain event.
     */
    public void setEventClassName(String eventClassName) { this.eventClassName = eventClassName; }
    /**
     * Returns the serialized event payload.
     */
    public JsonNode getEventData() { return eventData; }
    /**
     * Updates the serialized event payload.
     */
    public void setEventData(JsonNode eventData) { this.eventData = eventData; }
    /**
     * Returns the source service that published the event.
     */
    public String getProducerServiceName() { return producerServiceName; }
    /**
     * Updates the source service that published the event.
     */
    public void setProducerServiceName(String producerServiceName) { this.producerServiceName = producerServiceName; }
    /**
     * Returns whether the event is allowed to be consumed across consumer groups.
     */
    public boolean isAllowOtherGroupConsume() { return allowOtherGroupConsume; }
    /**
     * Updates whether the event is allowed to be consumed across consumer groups.
     */
    public void setAllowOtherGroupConsume(boolean allowOtherGroupConsume) { this.allowOtherGroupConsume = allowOtherGroupConsume; }
    /**
     * Returns the routing key used for async partition selection.
     */
    public String getRoutingKey() { return routingKey; }
    /**
     * Updates the routing key used for async partition selection.
     */
    public void setRoutingKey(String routingKey) { this.routingKey = routingKey; }
}
