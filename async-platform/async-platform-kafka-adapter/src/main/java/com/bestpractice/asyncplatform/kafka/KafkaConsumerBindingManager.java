package com.bestpractice.asyncplatform.kafka;

import com.bestpractice.asyncplatform.core.contract.SingleMqHandler;
import com.bestpractice.asyncplatform.core.contract.TaskPublisher;
import com.bestpractice.asyncplatform.core.model.ConsumeDisposition;
import com.bestpractice.asyncplatform.core.model.ConsumeResult;
import com.bestpractice.asyncplatform.core.model.DeliveryMetadata;
import com.bestpractice.asyncplatform.core.model.TaskEnvelope;
import com.bestpractice.asyncplatform.runtime.registry.DispatchGroup;
import com.bestpractice.asyncplatform.runtime.registry.HandlerBinding;
import com.bestpractice.asyncplatform.runtime.registry.HandlerRegistry;
import com.bestpractice.asyncplatform.runtime.registry.RegisteredHandler;
import com.bestpractice.asyncplatform.runtime.serialization.PlatformMessageSerializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.StringUtils;

/**
 * Owns Kafka consumer startup, polling and runtime routing for configured bindings.
 *
 * <p>Each dispatch group corresponds to a unique topic + consumer-group pair. Messages are
 * deserialized into platform envelopes, converted to the target payload type and then routed
 * to a {@link SingleMqHandler}. Retry, DLQ and poison handling are also centralized here.</p>
 */
@Slf4j
public class KafkaConsumerBindingManager implements SmartLifecycle {
    private final KafkaConsumerOptions options;
    private final HandlerRegistry handlerRegistry;
    private final PlatformMessageSerializer serializer;
    private final TaskPublisher taskPublisher;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final List<KafkaConsumer<String, byte[]>> consumers = new CopyOnWriteArrayList<>();
    private final AtomicBoolean running = new AtomicBoolean(false);

    public KafkaConsumerBindingManager(KafkaConsumerOptions options,
                                      HandlerRegistry handlerRegistry,
                                      PlatformMessageSerializer serializer,
                                      TaskPublisher taskPublisher) {
        this.options = options;
        this.handlerRegistry = handlerRegistry;
        this.serializer = serializer;
        this.taskPublisher = taskPublisher;
    }

    @Override
    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        for (DispatchGroup group : handlerRegistry.dispatchGroups()) {
            for (int i = 0; i < group.getConcurrency(); i++) {
                // Concurrency is implemented as one Kafka consumer per configured lane.
                KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(consumerProperties(group));
                consumer.subscribe(Collections.singletonList(group.getTopic()));
                consumers.add(consumer);
                executorService.submit(() -> runLoop(group, consumer));
            }
        }
        log.info("Started KafkaConsumerBindingManager with {} dispatch groups", handlerRegistry.dispatchGroups().size());
    }

    @Override
    public void stop() {
        running.set(false);
        consumers.forEach(KafkaConsumer::wakeup);
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    private void runLoop(DispatchGroup group, KafkaConsumer<String, byte[]> consumer) {
        try {
            while (running.get()) {
                try {
                    ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(options.pollTimeoutMs() == null ? 1000 : options.pollTimeoutMs()));
                    for (ConsumerRecord<String, byte[]> record : records) {
                        processRecord(group, record);
                    }
                    if (!records.isEmpty()) {
                        consumer.commitSync();
                    }
                } catch (org.apache.kafka.common.errors.WakeupException ex) {
                    if (running.get()) {
                        log.warn("Consumer wakeup while still running for {}:{}", group.getTopic(), group.getGroup(), ex);
                    }
                } catch (Exception ex) {
                    log.error("Consumer loop failure for {}:{}", group.getTopic(), group.getGroup(), ex);
                }
            }
        } finally {
            consumers.remove(consumer);
            consumer.close();
        }
    }

    private void processRecord(DispatchGroup group, ConsumerRecord<String, byte[]> record) {
        TaskEnvelope<JsonNode> rawEnvelope = serializer.deserialize(record.value());
        // The Kafka topic gets us to the group; subTopic selects the concrete business handler.
        RegisteredHandler<?> registeredHandler = group.resolve(rawEnvelope.getSubTopic()).orElse(null);
        if (registeredHandler == null) {
            log.warn("No handler route found for topic={}, group={}, subTopic={}", group.getTopic(), group.getGroup(), rawEnvelope.getSubTopic());
            return;
        }
        ConsumeResult result = invoke(registeredHandler, rawEnvelope);
        routeResult(rawEnvelope, registeredHandler.binding(), result);
    }

    private <T> ConsumeResult invoke(RegisteredHandler<T> registeredHandler, TaskEnvelope<JsonNode> rawEnvelope) {
        SingleMqHandler<T> handler = registeredHandler.handler();
        TaskEnvelope<T> typedEnvelope = serializer.convert(rawEnvelope, handler.payloadType());
        return handler.handle(typedEnvelope);
    }

    private void routeResult(TaskEnvelope<?> envelope, HandlerBinding binding, ConsumeResult result) {
        if (result == null || result.disposition() == ConsumeDisposition.SUCCESS || result.disposition() == ConsumeDisposition.SKIP) {
            return;
        }
        if (result.disposition() == ConsumeDisposition.RETRY && retryAllowed(envelope) && StringUtils.hasText(binding.getRetryTopic())) {
            publishTo(binding.getRetryTopic(), envelope, true);
            return;
        }
        if (result.disposition() == ConsumeDisposition.RETRY && !retryAllowed(envelope)) {
            log.warn("Retry limit reached. Routing message to DLQ if configured. taskId={}, topic={}, attempt={}, maxAttempts={}",
                    envelope.getTaskId(),
                    envelope.getTopic(),
                    currentAttempt(envelope),
                    maxAttempts(envelope));
        }
        if (result.disposition() == ConsumeDisposition.POISON && StringUtils.hasText(binding.getPoisonTopic())) {
            publishTo(binding.getPoisonTopic(), envelope, false);
            return;
        }
        if (StringUtils.hasText(binding.getDlqTopic())) {
            publishTo(binding.getDlqTopic(), envelope, false);
            return;
        }
        log.warn("Dropping failed message because no retry/DLQ target is configured. taskId={}, topic={}, reason={}",
                envelope.getTaskId(), envelope.getTopic(), result.reason());
    }

    private void publishTo(String topic, TaskEnvelope<?> envelope, boolean incrementAttempt) {
        TaskEnvelope<?> copy = envelope.copy();
        copy.setTopic(topic);
        if (incrementAttempt && copy.getDelivery() != null) {
            // Retry topics reuse the same business payload but move delivery bookkeeping forward.
            DeliveryMetadata delivery = copy.getDelivery().copy();
            delivery.setAttempt(delivery.getAttempt() + 1);
            delivery.setOriginTopic(envelope.getTopic());
            copy.setDelivery(delivery);
        }
        taskPublisher.publish(copy);
    }

    private boolean retryAllowed(TaskEnvelope<?> envelope) {
        return nextAttempt(envelope) < maxAttempts(envelope);
    }

    private int nextAttempt(TaskEnvelope<?> envelope) {
        return currentAttempt(envelope) + 1;
    }

    private int currentAttempt(TaskEnvelope<?> envelope) {
        return envelope.getDelivery() == null ? 0 : envelope.getDelivery().getAttempt();
    }

    private int maxAttempts(TaskEnvelope<?> envelope) {
        if (envelope.getDelivery() == null || envelope.getDelivery().getMaxAttempts() <= 0) {
            return 1;
        }
        return envelope.getDelivery().getMaxAttempts();
    }

    private Properties consumerProperties(DispatchGroup group) {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, options.bootstrapServers());
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, options.clientId() + "-" + group.getTopic());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, group.getGroup());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.FALSE);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        return properties;
    }
}
