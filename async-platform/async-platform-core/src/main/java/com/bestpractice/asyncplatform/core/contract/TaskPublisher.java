package com.bestpractice.asyncplatform.core.contract;

import com.bestpractice.asyncplatform.core.model.PublishResult;
import com.bestpractice.asyncplatform.core.model.TaskEnvelope;

/**
 * Platform-facing publish contract.
 *
 * <p>Business services publish envelopes through this abstraction instead of binding
 * themselves to a concrete transport such as Kafka.</p>
 */
public interface TaskPublisher {
    /**
     * Publishes one task envelope to the configured transport.
     *
     * @param envelope the canonical async message envelope to send
     * @return a publish result describing whether the send was accepted
     */
    PublishResult publish(TaskEnvelope<?> envelope);
}
