package com.bestpractice.asyncplatform.starter;

import java.util.List;
import java.util.Map;
import org.springframework.util.StringUtils;

/**
 * Fail-fast validation for starter configuration.
 *
 * <p>The goal is to fail during application startup instead of letting invalid topic,
 * handler or concurrency settings surface later at runtime.</p>
 */
public class AsyncPlatformConfigurationValidator {
    /**
     * Creates the validator and immediately validates the supplied configuration.
     */
    public AsyncPlatformConfigurationValidator(AsyncPlatformProperties properties) {
        validate(properties);
    }

    /**
     * Validates top-level Kafka, consumer and domain-event configuration.
     */
    private void validate(AsyncPlatformProperties properties) {
        requireText(properties.getKafka().getBootstrapServers(), "async-platform.kafka.bootstrap-servers must not be blank");
        requireText(properties.getKafka().getClientId(), "async-platform.kafka.client-id must not be blank");
        requirePositive(properties.getConsumer().getPollTimeoutMs(), "async-platform.consumer.poll-timeout-ms must be greater than 0");
        requireText(properties.getDomainEvents().getTopic(), "async-platform.domain-events.topic must not be blank");
        requireText(properties.getDomainEvents().getSubTopic(), "async-platform.domain-events.sub-topic must not be blank");

        for (Map.Entry<String, AsyncPlatformProperties.Binding> entry : properties.getConsumer().getBindings().entrySet()) {
            validateBinding(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Validates one binding definition.
     */
    private void validateBinding(String name, AsyncPlatformProperties.Binding binding) {
        if (!binding.isEnabled()) {
            return;
        }
        requireText(binding.getHandlerName(), "async-platform.consumer.bindings." + name + ".handler-name must not be blank");
        requireText(binding.getTopic(), "async-platform.consumer.bindings." + name + ".topic must not be blank");
        requireText(binding.getGroup(), "async-platform.consumer.bindings." + name + ".group must not be blank");
        requirePositive(binding.getConcurrency(), "async-platform.consumer.bindings." + name + ".concurrency must be greater than 0");
        List<String> subTopics = binding.getSubTopics();
        if (subTopics == null || subTopics.isEmpty()) {
            throw new IllegalStateException("async-platform.consumer.bindings." + name + ".sub-topics must not be empty");
        }
        boolean hasBlankSubTopic = subTopics.stream().anyMatch(subTopic -> !StringUtils.hasText(subTopic));
        if (hasBlankSubTopic) {
            throw new IllegalStateException("async-platform.consumer.bindings." + name + ".sub-topics must not contain blank values");
        }
    }

    /**
     * Requires a non-blank string value.
     */
    private void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * Requires a positive numeric value.
     */
    private void requirePositive(Number value, String message) {
        if (value == null || value.longValue() <= 0) {
            throw new IllegalStateException(message);
        }
    }
}
