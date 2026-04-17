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
 * Demo handler for the normal order-created task flow.
 */
@Slf4j
@Component
public class OrderCreatedHandler implements SingleMqHandler<OrderCreatedPayload> {
    /**
     * Shared observation store used by the demo and tests.
     */
    private final InMemoryDemoObservationStore observationStore;

    /**
     * Creates the normal order-created handler.
     */
    public OrderCreatedHandler(InMemoryDemoObservationStore observationStore) {
        this.observationStore = observationStore;
    }

    @Override
    /**
     * Returns the handler name used in binding configuration.
     */
    public String handlerName() {
        return "orderCreatedHandler";
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
     * Restricts the handler to the normal order-created sub-topic.
     */
    public Set<String> supportedSubTopics() {
        return Set.of("order.created");
    }

    @Override
    /**
     * Records successful handling of the normal order-created task.
     */
    public ConsumeResult handle(TaskEnvelope<OrderCreatedPayload> envelope) {
        log.info("Handled order created task. taskId={}, orderId={}, sku={}",
                envelope.getTaskId(),
                envelope.getPayload().orderId(),
                envelope.getPayload().sku());
        observationStore.recordTask(envelope.getPayload().orderId());
        return ConsumeResult.success();
    }
}
