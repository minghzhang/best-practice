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
 * Demo handler that consumes DLQ traffic and records it in the observation store.
 */
@Slf4j
@Component
public class DeadLetterObservationHandler implements SingleMqHandler<OrderCreatedPayload> {
    /**
     * Shared observation store used by the demo and tests.
     */
    private final InMemoryDemoObservationStore observationStore;

    /**
     * Creates the dead-letter observation handler.
     */
    public DeadLetterObservationHandler(InMemoryDemoObservationStore observationStore) {
        this.observationStore = observationStore;
    }

    @Override
    /**
     * Returns the handler name used in binding configuration.
     */
    public String handlerName() {
        return "deadLetterObservationHandler";
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
     * Restricts the handler to the demo DLQ sub-topics.
     */
    public Set<String> supportedSubTopics() {
        return Set.of("order.retry.once", "order.dlq");
    }

    @Override
    /**
     * Records that the message was observed on the dead-letter path.
     */
    public ConsumeResult handle(TaskEnvelope<OrderCreatedPayload> envelope) {
        String orderId = envelope.getPayload().orderId();
        log.info("Observed dead-letter order task. taskId={}, orderId={}, topic={}",
                envelope.getTaskId(), orderId, envelope.getTopic());
        observationStore.recordDeadLetterTask(orderId);
        return ConsumeResult.success();
    }
}
