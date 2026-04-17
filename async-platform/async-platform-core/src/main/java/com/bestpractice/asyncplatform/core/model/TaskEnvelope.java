package com.bestpractice.asyncplatform.core.model;

import java.time.Instant;

/**
 * Canonical async message envelope used across the platform.
 *
 * <p>The payload is wrapped together with tracking, routing and delivery metadata so
 * business code can stay transport-agnostic while the runtime still has enough
 * information to route, retry and observe the message.</p>
 */
public class TaskEnvelope<T> {
    /**
     * Unique identifier of the task instance.
     */
    private String taskId;
    /**
     * Trace identifier carried across async boundaries for observability.
     */
    private String traceId;
    /**
     * Human-readable logical task name.
     */
    private String name;
    /**
     * Physical transport topic used to carry the message.
     */
    private String topic;
    /**
     * Logical route inside the topic used to select the business handler.
     */
    private String subTopic;
    /**
     * Fully qualified payload type name kept for diagnostics and schema inspection.
     */
    private String payloadType;
    /**
     * Schema version of the envelope or payload contract.
     */
    private int schemaVersion = 1;
    /**
     * Partitioning key used by the transport.
     */
    private String key;
    /**
     * Business payload carried by the task.
     */
    private T payload;
    /**
     * Cross-cutting tracking metadata such as request and tenant information.
     */
    private TrackingMetadata tracking = new TrackingMetadata();
    /**
     * Routing hints used by the runtime.
     */
    private RoutingMetadata routing = new RoutingMetadata();
    /**
     * Delivery bookkeeping used for retry, delay and dedupe.
     */
    private DeliveryMetadata delivery = new DeliveryMetadata();
    /**
     * Timestamp when the envelope was created.
     */
    private Instant producedAt = Instant.now();

    /**
     * Returns the unique task identifier.
     */
    public String getTaskId() { return taskId; }
    /**
     * Updates the unique task identifier.
     */
    public void setTaskId(String taskId) { this.taskId = taskId; }
    /**
     * Returns the cross-boundary trace identifier.
     */
    public String getTraceId() { return traceId; }
    /**
     * Updates the cross-boundary trace identifier.
     */
    public void setTraceId(String traceId) { this.traceId = traceId; }
    /**
     * Returns the logical task name.
     */
    public String getName() { return name; }
    /**
     * Updates the logical task name.
     */
    public void setName(String name) { this.name = name; }
    /**
     * Returns the physical transport topic.
     */
    public String getTopic() { return topic; }
    /**
     * Updates the physical transport topic.
     */
    public void setTopic(String topic) { this.topic = topic; }
    /**
     * Returns the logical sub-topic used for handler routing.
     */
    public String getSubTopic() { return subTopic; }
    /**
     * Updates the logical sub-topic used for handler routing.
     */
    public void setSubTopic(String subTopic) { this.subTopic = subTopic; }
    /**
     * Returns the payload type name.
     */
    public String getPayloadType() { return payloadType; }
    /**
     * Updates the payload type name.
     */
    public void setPayloadType(String payloadType) { this.payloadType = payloadType; }
    /**
     * Returns the schema version carried by the envelope.
     */
    public int getSchemaVersion() { return schemaVersion; }
    /**
     * Updates the schema version carried by the envelope.
     */
    public void setSchemaVersion(int schemaVersion) { this.schemaVersion = schemaVersion; }
    /**
     * Returns the transport partition key.
     */
    public String getKey() { return key; }
    /**
     * Updates the transport partition key.
     */
    public void setKey(String key) { this.key = key; }
    /**
     * Returns the business payload.
     */
    public T getPayload() { return payload; }
    /**
     * Updates the business payload.
     */
    public void setPayload(T payload) { this.payload = payload; }
    /**
     * Returns the tracking metadata attached to the message.
     */
    public TrackingMetadata getTracking() { return tracking; }
    /**
     * Updates the tracking metadata attached to the message.
     */
    public void setTracking(TrackingMetadata tracking) { this.tracking = tracking; }
    /**
     * Returns the routing metadata attached to the message.
     */
    public RoutingMetadata getRouting() { return routing; }
    /**
     * Updates the routing metadata attached to the message.
     */
    public void setRouting(RoutingMetadata routing) { this.routing = routing; }
    /**
     * Returns the delivery metadata attached to the message.
     */
    public DeliveryMetadata getDelivery() { return delivery; }
    /**
     * Updates the delivery metadata attached to the message.
     */
    public void setDelivery(DeliveryMetadata delivery) { this.delivery = delivery; }
    /**
     * Returns the envelope creation timestamp.
     */
    public Instant getProducedAt() { return producedAt; }
    /**
     * Updates the envelope creation timestamp.
     */
    public void setProducedAt(Instant producedAt) { this.producedAt = producedAt; }

    /**
     * Creates a shallow copy of the envelope while cloning delivery metadata so
     * retry bookkeeping can mutate attempt counters without changing the original message.
     */
    public TaskEnvelope<T> copy() {
        TaskEnvelope<T> copy = new TaskEnvelope<>();
        copy.taskId = taskId;
        copy.traceId = traceId;
        copy.name = name;
        copy.topic = topic;
        copy.subTopic = subTopic;
        copy.payloadType = payloadType;
        copy.schemaVersion = schemaVersion;
        copy.key = key;
        copy.payload = payload;
        copy.tracking = tracking;
        copy.routing = routing;
        copy.delivery = delivery == null ? null : delivery.copy();
        copy.producedAt = producedAt;
        return copy;
    }
}
