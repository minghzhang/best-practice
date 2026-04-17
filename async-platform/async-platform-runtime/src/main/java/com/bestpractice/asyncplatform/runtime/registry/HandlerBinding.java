package com.bestpractice.asyncplatform.runtime.registry;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
/**
 * Configuration-side description of one handler binding.
 *
 * <p>A binding answers the question: which handler consumes which topic/group/sub-topics,
 * and where should failures be routed?</p>
 */
public class HandlerBinding {
    /**
     * Configuration name of the binding itself.
     */
    private String name;
    /**
     * Whether the binding is enabled.
     */
    private boolean enabled;
    /**
     * Name of the handler bean referenced by this binding.
     */
    private String handlerName;
    /**
     * Topic consumed by the handler.
     */
    private String topic;
    /**
     * Consumer group used for the topic subscription.
     */
    private String group;
    /**
     * Logical sub-topics inside the topic that should be routed to this handler.
     */
    private List<String> subTopics;
    @Builder.Default
    /**
     * Number of parallel consumer lanes requested for this binding.
     */
    private int concurrency = 1;
    /**
     * Topic used when the handler requests retry.
     */
    private String retryTopic;
    /**
     * Dead-letter topic used when the handler requests DLQ or retries are exhausted.
     */
    private String dlqTopic;
    /**
     * Topic used for poison messages.
     */
    private String poisonTopic;
}
