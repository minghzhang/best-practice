package com.bestpractice.asyncplatform.starter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * External configuration model exposed by the Spring Boot starter.
 *
 * <p>Business applications should be able to wire the platform through configuration alone:
 * Kafka connection settings, consumer topology and domain-event transport settings all live here.</p>
 */
@Data
@ConfigurationProperties(prefix = "async-platform")
public class AsyncPlatformProperties {
    /**
     * Kafka connection and producer settings.
     */
    private Kafka kafka = new Kafka();
    /**
     * Consumer topology and poll-loop settings.
     */
    private Consumer consumer = new Consumer();
    /**
     * Domain-event transport settings.
     */
    private DomainEvents domainEvents = new DomainEvents();

    @Data
    public static class Kafka {
        /**
         * Kafka bootstrap server list.
         */
        private String bootstrapServers;
        /**
         * Client identifier prefix used by producers and consumers.
         */
        private String clientId = "async-platform";
        /**
         * Producer-specific tuning options.
         */
        private Producer producer = new Producer();
    }

    @Data
    public static class Producer {
        /**
         * Producer acknowledgement policy.
         */
        private String acks = "all";
        /**
         * Producer retry count.
         */
        private Integer retries = 3;
        /**
         * Producer linger time in milliseconds.
         */
        private Integer lingerMs = 5;
        /**
         * Producer batch size in bytes.
         */
        private Integer batchSize = 16384;
        /**
         * Producer request timeout in milliseconds.
         */
        private Integer requestTimeoutMs = 3000;
    }

    @Data
    public static class Consumer {
        /**
         * Poll timeout in milliseconds for each consumer loop.
         */
        private Long pollTimeoutMs = 1000L;
        // Each binding describes one handler-to-topic/group relationship.
        private Map<String, Binding> bindings = new LinkedHashMap<>();
    }

    @Data
    public static class Binding {
        /**
         * Whether the binding is enabled.
         */
        private boolean enabled = true;
        /**
         * Handler bean name referenced by this binding.
         */
        private String handlerName;
        /**
         * Topic consumed by the binding.
         */
        private String topic;
        /**
         * Consumer group used by the binding.
         */
        private String group;
        /**
         * Sub-topics routed to the target handler.
         */
        private List<String> subTopics;
        /**
         * Number of parallel consumer lanes.
         */
        private Integer concurrency = 1;
        /**
         * Retry topic used when the handler asks for RETRY.
         */
        private String retryTopic;
        /**
         * Dead-letter topic used when retries are exhausted or DLQ is requested.
         */
        private String dlqTopic;
        /**
         * Poison topic used when the message is considered unrecoverable.
         */
        private String poisonTopic;
    }

    @Data
    public static class DomainEvents {
        /**
         * Topic used for asynchronous domain-event transport.
         */
        private String topic = "async_platform_events";
        /**
         * Sub-topic reserved for transported domain events.
         */
        private String subTopic = "domain.event";
        /**
         * Whether in-process local dispatch is enabled.
         */
        private boolean allowLocalDispatch = true;
    }
}
