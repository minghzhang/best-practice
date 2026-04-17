package com.bestpractice.asyncplatform.core.contract;

import com.bestpractice.asyncplatform.core.model.ConsumeResult;
import com.bestpractice.asyncplatform.core.model.TaskEnvelope;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Set;

/**
 * Business-facing consumer contract.
 *
 * <p>Handlers should focus on payload processing only. Topic/group/concurrency and
 * retry/DLQ routing are platform concerns configured outside the handler.</p>
 */
public interface SingleMqHandler<T> {
    /**
     * Returns the stable handler identifier used by configuration bindings.
     *
     * <p>The platform uses this name to match external binding config with the
     * Spring bean that should handle the message.</p>
     */
    String handlerName();

    /**
     * Declares the payload type expected by this handler.
     *
     * <p>The runtime first deserializes Kafka payloads into a generic envelope and
     * then uses this type reference to convert the payload into the business type
     * just before invocation.</p>
     */
    TypeReference<T> payloadType();

    /**
     * Allows a handler to declare the sub-topics it is willing to process.
     * Returning an empty set means the binding configuration is the source of truth.
     */
    default Set<String> supportedSubTopics() {
        return Set.of();
    }

    /**
     * Processes one platform envelope.
     *
     * <p>The handler returns a {@link ConsumeResult} instead of throwing transport-aware
     * exceptions so the runtime can decide whether to acknowledge, retry, dead-letter or
     * poison-route the message.</p>
     */
    ConsumeResult handle(TaskEnvelope<T> envelope);
}
