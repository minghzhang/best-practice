package com.bestpractice.asyncplatform.kafka;

import lombok.Builder;

/**
 * Immutable consumer-side transport options.
 *
 * @param bootstrapServers Kafka bootstrap server list
 * @param clientId client identifier prefix used by created consumers
 * @param pollTimeoutMs poll timeout in milliseconds for each consumer loop
 */
@Builder
public record KafkaConsumerOptions(String bootstrapServers,
                                   String clientId,
                                   Long pollTimeoutMs) {
}
