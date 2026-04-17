package com.bestpractice.asyncplatform.demo.web;

import com.bestpractice.asyncplatform.core.contract.TaskPublisher;
import com.bestpractice.asyncplatform.core.domain.DomainEventDeliveryMode;
import com.bestpractice.asyncplatform.core.domain.DomainEventPublisher;
import com.bestpractice.asyncplatform.core.model.PublishResult;
import com.bestpractice.asyncplatform.core.model.TaskEnvelope;
import com.bestpractice.asyncplatform.demo.event.OrderCreatedEvent;
import com.bestpractice.asyncplatform.demo.observation.InMemoryDemoObservationStore;
import com.bestpractice.asyncplatform.demo.task.OrderCreatedPayload;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Small demo surface used to exercise the platform end to end.
 */
@RestController
@RequestMapping("/api/demo")
public class DemoController {
    /**
     * Task publisher used by the demo task endpoints.
     */
    private final TaskPublisher taskPublisher;
    /**
     * Domain-event publisher used by the demo event endpoints.
     */
    private final DomainEventPublisher domainEventPublisher;
    /**
     * Observation store exposed by the inspection endpoint.
     */
    private final InMemoryDemoObservationStore observationStore;

    /**
     * Creates the demo controller.
     */
    public DemoController(TaskPublisher taskPublisher,
                          DomainEventPublisher domainEventPublisher,
                          InMemoryDemoObservationStore observationStore) {
        this.taskPublisher = taskPublisher;
        this.domainEventPublisher = domainEventPublisher;
        this.observationStore = observationStore;
    }

    @PostMapping("/tasks/orders/{orderId}")
    /**
     * Publishes a normal order-created task.
     */
    public PublishResult publishTask(@PathVariable String orderId) {
        return publishTask(orderId, "order.created", "order-created-task");
    }

    @PostMapping("/tasks/retry/orders/{orderId}")
    /**
     * Publishes a task that intentionally retries once before succeeding.
     */
    public PublishResult publishRetryTask(@PathVariable String orderId) {
        return publishTask(orderId, "order.retry.once", "order-retry-task");
    }

    @PostMapping("/tasks/dlq/orders/{orderId}")
    /**
     * Publishes a task that is expected to enter the dead-letter path.
     */
    public PublishResult publishDlqTask(@PathVariable String orderId) {
        return publishTask(orderId, "order.dlq", "order-dlq-task");
    }

    /**
     * Builds and publishes one demo task envelope.
     */
    private PublishResult publishTask(String orderId, String subTopic, String taskName) {
        // The demo publishes different business paths by varying only the subTopic and task name.
        TaskEnvelope<OrderCreatedPayload> envelope = new TaskEnvelope<>();
        envelope.setTaskId(UUID.randomUUID().toString());
        envelope.setTraceId(UUID.randomUUID().toString());
        envelope.setName(taskName);
        envelope.setTopic("async_platform_tasks");
        envelope.setSubTopic(subTopic);
        envelope.setPayloadType(OrderCreatedPayload.class.getName());
        envelope.setKey(orderId);
        envelope.setPayload(new OrderCreatedPayload(orderId, "demo-sku", Instant.now()));
        return taskPublisher.publish(envelope);
    }

    @PostMapping("/events/orders/{orderId}")
    /**
     * Publishes one order-created domain event in BOTH delivery mode.
     */
    public PublishResult publishEvent(@PathVariable String orderId) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                orderId,
                Instant.now(),
                "demo-controller"
        );
        return domainEventPublisher.publish(event, DomainEventDeliveryMode.BOTH);
    }

    @GetMapping("/observations")
    /**
     * Returns the current in-memory observation snapshot.
     */
    public Map<String, Object> observations() {
        return observationStore.snapshot();
    }
}
