package com.bestpractice.asyncplatform.kafka;

import com.bestpractice.asyncplatform.core.contract.TaskPublisher;
import com.bestpractice.asyncplatform.core.model.PublishResult;
import com.bestpractice.asyncplatform.core.model.TaskEnvelope;
import com.bestpractice.asyncplatform.runtime.serialization.PlatformMessageSerializer;
import java.io.Closeable;
import java.time.Duration;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * Kafka-backed implementation of the platform publish contract.
 *
 * <p>Only the adapter knows about Kafka classes. Upstream modules publish through
 * {@link com.bestpractice.asyncplatform.core.contract.TaskPublisher}.</p>
 */
public class KafkaTaskPublisher implements TaskPublisher, Closeable {
    /**
     * Underlying Kafka producer used to send serialized envelopes.
     */
    private final Producer<String, byte[]> producer;
    /**
     * Serializer used to convert the platform envelope into raw bytes.
     */
    private final PlatformMessageSerializer serializer;
    /**
     * Mapper that copies selected envelope metadata into Kafka headers.
     */
    private final KafkaHeaderMapper headerMapper;
    /**
     * Maximum time to wait for the broker acknowledgement.
     */
    private final Duration requestTimeout;

    /**
     * Creates the Kafka-backed task publisher.
     *
     * @param options producer transport settings
     * @param serializer serializer used for envelope payloads
     * @param headerMapper mapper used for Kafka headers
     */
    public KafkaTaskPublisher(KafkaProducerOptions options,
                              PlatformMessageSerializer serializer,
                              KafkaHeaderMapper headerMapper) {
        this.producer = new KafkaProducer<>(producerProperties(options));
        this.serializer = serializer;
        this.headerMapper = headerMapper;
        this.requestTimeout = Duration.ofMillis(options.requestTimeoutMs() == null ? 3000 : options.requestTimeoutMs());
    }

    @Override
    /**
     * Publishes one task envelope to Kafka and waits for the send result.
     */
    public PublishResult publish(TaskEnvelope<?> envelope) {
        try {
            ProducerRecord<String, byte[]> record = new ProducerRecord<>(
                    envelope.getTopic(),
                    envelope.getKey(),
                    serializer.serialize(envelope)
            );
            headerMapper.applyHeaders(record.headers(), envelope);
            RecordMetadata metadata = producer.send(record).get(requestTimeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
            return PublishResult.success(envelope.getTaskId(), metadata.topic());
        } catch (Exception ex) {
            return PublishResult.failure(envelope.getTaskId(), envelope.getTopic(), ex.getMessage());
        }
    }

    /**
     * Builds Kafka producer properties from the platform-level producer options.
     */
    private Properties producerProperties(KafkaProducerOptions options) {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, options.bootstrapServers());
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, options.clientId());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        properties.put(ProducerConfig.ACKS_CONFIG, options.acks() == null ? "all" : options.acks());
        properties.put(ProducerConfig.RETRIES_CONFIG, options.retries() == null ? 3 : options.retries());
        properties.put(ProducerConfig.LINGER_MS_CONFIG, options.lingerMs() == null ? 5 : options.lingerMs());
        properties.put(ProducerConfig.BATCH_SIZE_CONFIG, options.batchSize() == null ? 16384 : options.batchSize());
        properties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, options.requestTimeoutMs() == null ? 3000 : options.requestTimeoutMs());
        return properties;
    }

    @Override
    /**
     * Closes the underlying Kafka producer.
     */
    public void close() {
        producer.close();
    }
}
