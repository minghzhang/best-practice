package com.bestpractice.asyncplatform.demo;

import static org.assertj.core.api.Assertions.assertThat;

import com.bestpractice.asyncplatform.core.contract.TaskPublisher;
import com.bestpractice.asyncplatform.core.domain.DomainEventDeliveryMode;
import com.bestpractice.asyncplatform.core.domain.DomainEventPublisher;
import com.bestpractice.asyncplatform.core.model.DeliveryMetadata;
import com.bestpractice.asyncplatform.core.model.PublishResult;
import com.bestpractice.asyncplatform.core.model.TaskEnvelope;
import com.bestpractice.asyncplatform.demo.event.OrderCreatedEvent;
import com.bestpractice.asyncplatform.demo.observation.InMemoryDemoObservationStore;
import com.bestpractice.asyncplatform.demo.task.OrderCreatedPayload;
import java.time.Instant;
import java.util.UUID;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "async-platform.consumer.poll-timeout-ms=100"
        })
@EmbeddedKafka(
        bootstrapServersProperty = "async-platform.kafka.bootstrap-servers",
        partitions = 1,
        topics = {
                "async_platform_tasks",
                "async_platform_tasks_retry",
                "async_platform_tasks_dlq",
                "async_platform_tasks_poison",
                "async_platform_events",
                "async_platform_events_retry",
                "async_platform_events_dlq",
                "async_platform_events_poison"
        },
        brokerProperties = {
                "auto.create.topics.enable=true",
                "group.initial.rebalance.delay.ms=0"
        }
)
@DirtiesContext
class AsyncPlatformDemoIntegrationTest {
    @Autowired
    private TaskPublisher taskPublisher;

    @Autowired
    private DomainEventPublisher domainEventPublisher;

    @Autowired
    private InMemoryDemoObservationStore observationStore;

    @BeforeEach
    void setUp() {
        observationStore.reset();
    }

    @Test
    void shouldConsumePublishedTask() {
        String orderId = "order-task-it-1";
        TaskEnvelope<OrderCreatedPayload> envelope = newTaskEnvelope(orderId, "order.created", "order-created-task");

        PublishResult result = taskPublisher.publish(envelope);
        assertThat(result.success()).isTrue();

        Awaitility.await()
                .untilAsserted(() -> assertThat(observationStore.hasHandledTask(orderId)).isTrue());
    }

    @Test
    void shouldDispatchDomainEventLocallyAndViaMq() {
        String orderId = "order-event-it-1";
        PublishResult result = domainEventPublisher.publish(
                new OrderCreatedEvent(UUID.randomUUID().toString(), orderId, Instant.now(), "integration-test"),
                DomainEventDeliveryMode.BOTH
        );
        assertThat(result.success()).isTrue();

        Awaitility.await()
                .untilAsserted(() -> assertThat(observationStore.domainEventCount(orderId)).isGreaterThanOrEqualTo(2));
    }

    @Test
    void shouldRetryOnceAndConsumeFromRetryTopic() {
        String orderId = "order-retry-it-1";
        PublishResult result = taskPublisher.publish(newTaskEnvelope(orderId, "order.retry.once", "order-retry-task"));
        assertThat(result.success()).isTrue();

        Awaitility.await()
                .untilAsserted(() -> {
                    assertThat(observationStore.hasRetriedTask(orderId)).isTrue();
                    assertThat(observationStore.retryAttempts(orderId)).isGreaterThanOrEqualTo(2);
                    assertThat(observationStore.hasDeadLetterTask(orderId)).isFalse();
                });
    }

    @Test
    void shouldRouteToDlqWhenRetryBudgetExhausted() {
        String orderId = "order-retry-dlq-it-1";
        TaskEnvelope<OrderCreatedPayload> envelope = newTaskEnvelope(orderId, "order.retry.once", "order-retry-exhausted-task");
        DeliveryMetadata delivery = new DeliveryMetadata();
        delivery.setMaxAttempts(1);
        envelope.setDelivery(delivery);

        PublishResult result = taskPublisher.publish(envelope);
        assertThat(result.success()).isTrue();

        Awaitility.await()
                .untilAsserted(() -> {
                    assertThat(observationStore.hasDeadLetterTask(orderId)).isTrue();
                    assertThat(observationStore.hasRetriedTask(orderId)).isFalse();
                    assertThat(observationStore.retryAttempts(orderId)).isEqualTo(1);
                });
    }

    @Test
    void shouldRouteExplicitDlqResultToDlqTopic() {
        String orderId = "order-dlq-it-1";
        PublishResult result = taskPublisher.publish(newTaskEnvelope(orderId, "order.dlq", "order-dlq-task"));
        assertThat(result.success()).isTrue();

        Awaitility.await()
                .untilAsserted(() -> assertThat(observationStore.hasDeadLetterTask(orderId)).isTrue());
    }

    private TaskEnvelope<OrderCreatedPayload> newTaskEnvelope(String orderId, String subTopic, String taskName) {
        TaskEnvelope<OrderCreatedPayload> envelope = new TaskEnvelope<>();
        envelope.setTaskId(UUID.randomUUID().toString());
        envelope.setTraceId(UUID.randomUUID().toString());
        envelope.setName(taskName);
        envelope.setTopic("async_platform_tasks");
        envelope.setSubTopic(subTopic);
        envelope.setPayloadType(OrderCreatedPayload.class.getName());
        envelope.setKey(orderId);
        envelope.setPayload(new OrderCreatedPayload(orderId, "sku-it-1", Instant.now()));
        return envelope;
    }
}
