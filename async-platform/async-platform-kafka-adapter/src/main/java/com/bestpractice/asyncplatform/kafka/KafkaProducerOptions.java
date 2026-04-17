package com.bestpractice.asyncplatform.kafka;

import lombok.Builder;

/**
 * Immutable producer-side transport options.
 *
 * @param bootstrapServers Kafka bootstrap server list
 * @param clientId Kafka producer client identifier
 * @param acks acknowledgement policy
 * @param retries producer retry count
 * @param lingerMs linger duration in milliseconds
 * @param batchSize batch size in bytes
 * @param requestTimeoutMs request timeout in milliseconds
 */
@Builder
public record KafkaProducerOptions(String bootstrapServers,
                                   String clientId,
                                   String acks,
                                   Integer retries,
                                   Integer lingerMs,
                                   Integer batchSize,
                                   Integer requestTimeoutMs) {
}
