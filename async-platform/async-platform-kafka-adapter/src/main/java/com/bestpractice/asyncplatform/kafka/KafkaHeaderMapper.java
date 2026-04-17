package com.bestpractice.asyncplatform.kafka;

import com.bestpractice.asyncplatform.core.model.TaskEnvelope;
import java.nio.charset.StandardCharsets;
import org.apache.kafka.common.header.Headers;

/**
 * Copies selected platform metadata into Kafka headers.
 */
public class KafkaHeaderMapper {
    /**
     * Writes trace and schema-related metadata from the envelope into Kafka headers.
     */
    public void applyHeaders(Headers headers, TaskEnvelope<?> envelope) {
        add(headers, "traceId", envelope.getTraceId());
        add(headers, "taskId", envelope.getTaskId());
        add(headers, "subTopic", envelope.getSubTopic());
        add(headers, "payloadType", envelope.getPayloadType());
        add(headers, "schemaVersion", String.valueOf(envelope.getSchemaVersion()));
        if (envelope.getDelivery() != null) {
            add(headers, "attempt", String.valueOf(envelope.getDelivery().getAttempt()));
        }
    }

    /**
     * Adds one UTF-8 header value if the supplied value is present.
     */
    private void add(Headers headers, String key, String value) {
        if (value != null) {
            headers.add(key, value.getBytes(StandardCharsets.UTF_8));
        }
    }
}
