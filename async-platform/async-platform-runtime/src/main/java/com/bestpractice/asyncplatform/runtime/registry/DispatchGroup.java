package com.bestpractice.asyncplatform.runtime.registry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;

@Getter
/**
 * Runtime view of one Kafka topic + consumer-group lane.
 *
 * <p>A dispatch group owns the sub-topic routes that are consumed together by the same
 * physical consumer container.</p>
 */
public class DispatchGroup {
    /**
     * Topic consumed by this dispatch group.
     */
    private final String topic;
    /**
     * Consumer group used by this dispatch group.
     */
    private final String group;
    /**
     * Number of parallel consumer lanes configured for this group.
     */
    private final int concurrency;
    /**
     * Mapping from logical sub-topic to the registered business handler.
     */
    private final Map<String, RegisteredHandler<?>> routes = new LinkedHashMap<>();

    /**
     * Creates one dispatch group for a topic + consumer-group pair.
     */
    public DispatchGroup(String topic, String group, int concurrency) {
        this.topic = topic;
        this.group = group;
        this.concurrency = concurrency;
    }

    /**
     * Registers one sub-topic route inside this dispatch group.
     *
     * <p>Duplicate routes are rejected because runtime dispatch must be deterministic.</p>
     */
    public void register(String subTopic, RegisteredHandler<?> handler) {
        if (routes.putIfAbsent(subTopic, handler) != null) {
            throw new IllegalStateException("Duplicate subTopic route detected for " + topic + ":" + group + ":" + subTopic);
        }
    }

    /**
     * Resolves the handler registered for the given sub-topic.
     */
    public Optional<RegisteredHandler<?>> resolve(String subTopic) {
        return Optional.ofNullable(routes.get(subTopic));
    }
}
