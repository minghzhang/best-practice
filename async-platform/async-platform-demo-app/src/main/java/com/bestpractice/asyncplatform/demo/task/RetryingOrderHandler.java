package com.bestpractice.asyncplatform.demo.task;

import com.bestpractice.asyncplatform.core.contract.SingleMqHandler;
import com.bestpractice.asyncplatform.core.model.ConsumeResult;
import com.bestpractice.asyncplatform.core.model.TaskEnvelope;
import com.bestpractice.asyncplatform.demo.observation.InMemoryDemoObservationStore;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Demo handler that fails once and then succeeds to exercise the retry flow.
 */
@Slf4j
@Component
public class RetryingOrderHandler implements SingleMqHandler<OrderCreatedPayload> {
    /**
     * Shared observation store used by the demo and tests.
     */
    private final InMemoryDemoObservationStore observationStore;

    /**
     * Creates the retrying handler.
     */
    public RetryingOrderHandler(InMemoryDemoObservationStore observationStore) {
        this.observationStore = observationStore;
    }

    @Override
    /**
     * Returns the handler name used in binding configuration.
     */
    public String handlerName() {
        return "retryingOrderHandler";
    }

    @Override
    /**
     * Declares the payload type expected by this handler.
     */
    public TypeReference<OrderCreatedPayload> payloadType() {
        return new TypeReference<>() {};
    }

    @Override
    /**
     * Restricts the handler to the demo retry sub-topic.
     */
    public Set<String> supportedSubTopics() {
        return Set.of("order.retry.once");
    }

    @Override
    /**
     * Requests retry on the first attempt and succeeds on later attempts.
     */
    public ConsumeResult handle(TaskEnvelope<OrderCreatedPayload> envelope) {
        int attempt = envelope.getDelivery() == null ? 0 : envelope.getDelivery().getAttempt();
        String orderId = envelope.getPayload().orderId();
        observationStore.recordRetryAttempt(orderId);
        if (attempt == 0) {
            log.info("Triggering retry for order. taskId={}, orderId={}, topic={}", envelope.getTaskId(), orderId, envelope.getTopic());
            return ConsumeResult.retry("simulated first-attempt retry");
        }
        log.info("Handled retried order task. taskId={}, orderId={}, topic={}, attempt={}",
                envelope.getTaskId(), orderId, envelope.getTopic(), attempt);
        observationStore.recordRetriedTask(orderId);
        return ConsumeResult.success();
    }
}
