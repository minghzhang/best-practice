package com.bestpractice.asyncplatform.demo.task;

import com.bestpractice.asyncplatform.core.contract.SingleMqHandler;
import com.bestpractice.asyncplatform.core.model.ConsumeResult;
import com.bestpractice.asyncplatform.core.model.TaskEnvelope;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Demo handler that always requests DLQ routing for the received order task.
 */
@Slf4j
@Component
public class DlqSourceOrderHandler implements SingleMqHandler<OrderCreatedPayload> {
    @Override
    /**
     * Returns the handler name used in binding configuration.
     */
    public String handlerName() {
        return "dlqSourceOrderHandler";
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
     * Restricts the handler to the demo DLQ source sub-topic.
     */
    public Set<String> supportedSubTopics() {
        return Set.of("order.dlq");
    }

    @Override
    /**
     * Returns a DLQ result to demonstrate dead-letter routing.
     */
    public ConsumeResult handle(TaskEnvelope<OrderCreatedPayload> envelope) {
        log.info("Routing order task to DLQ. taskId={}, orderId={}, topic={}",
                envelope.getTaskId(), envelope.getPayload().orderId(), envelope.getTopic());
        return ConsumeResult.dlq("simulated dead-letter routing");
    }
}
