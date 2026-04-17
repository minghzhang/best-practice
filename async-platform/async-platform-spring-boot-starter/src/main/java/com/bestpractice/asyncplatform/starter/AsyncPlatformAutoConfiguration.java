package com.bestpractice.asyncplatform.starter;

import com.bestpractice.asyncplatform.core.contract.SingleMqHandler;
import com.bestpractice.asyncplatform.core.contract.TaskPublisher;
import com.bestpractice.asyncplatform.core.domain.DomainEventPublisher;
import com.bestpractice.asyncplatform.core.domain.DomainEventSubscriber;
import com.bestpractice.asyncplatform.kafka.KafkaConsumerBindingManager;
import com.bestpractice.asyncplatform.kafka.KafkaConsumerOptions;
import com.bestpractice.asyncplatform.kafka.KafkaHeaderMapper;
import com.bestpractice.asyncplatform.kafka.KafkaProducerOptions;
import com.bestpractice.asyncplatform.kafka.KafkaTaskPublisher;
import com.bestpractice.asyncplatform.runtime.domain.DefaultDomainEventPublisher;
import com.bestpractice.asyncplatform.runtime.domain.DefaultDomainEventSubscriberRegistry;
import com.bestpractice.asyncplatform.runtime.domain.DomainEventSubscriberRegistry;
import com.bestpractice.asyncplatform.runtime.registry.HandlerBinding;
import com.bestpractice.asyncplatform.runtime.registry.HandlerRegistry;
import com.bestpractice.asyncplatform.runtime.serialization.JacksonPlatformMessageSerializer;
import com.bestpractice.asyncplatform.runtime.serialization.PlatformMessageSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot entry point for the platform.
 *
 * <p>This class wires together the core runtime, Kafka adapter and domain-event bridge so
 * an application only needs dependencies + configuration to start using the platform.</p>
 */
@AutoConfiguration
@EnableConfigurationProperties(AsyncPlatformProperties.class)
public class AsyncPlatformAutoConfiguration {
    /**
     * Creates the fail-fast configuration validator.
     */
    @Bean
    @ConditionalOnMissingBean
    public AsyncPlatformConfigurationValidator asyncPlatformConfigurationValidator(AsyncPlatformProperties properties) {
        return new AsyncPlatformConfigurationValidator(properties);
    }

    /**
     * Creates the default JSON serializer used by the platform runtime.
     */
    @Bean
    @ConditionalOnMissingBean
    public PlatformMessageSerializer platformMessageSerializer(ObjectMapper objectMapper) {
        return new JacksonPlatformMessageSerializer(objectMapper.copy());
    }

    /**
     * Creates the Kafka header mapper used by the Kafka adapter.
     */
    @Bean
    @ConditionalOnMissingBean
    public KafkaHeaderMapper kafkaHeaderMapper() {
        return new KafkaHeaderMapper();
    }

    /**
     * Creates the default task publisher backed by Kafka.
     */
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(TaskPublisher.class)
    public TaskPublisher taskPublisher(AsyncPlatformProperties properties,
                                       PlatformMessageSerializer serializer,
                                       KafkaHeaderMapper headerMapper) {
        KafkaProducerOptions options = KafkaProducerOptions.builder()
                .bootstrapServers(properties.getKafka().getBootstrapServers())
                .clientId(properties.getKafka().getClientId())
                .acks(properties.getKafka().getProducer().getAcks())
                .retries(properties.getKafka().getProducer().getRetries())
                .lingerMs(properties.getKafka().getProducer().getLingerMs())
                .batchSize(properties.getKafka().getProducer().getBatchSize())
                .requestTimeoutMs(properties.getKafka().getProducer().getRequestTimeoutMs())
                .build();
        return new KafkaTaskPublisher(options, serializer, headerMapper);
    }

    /**
     * Creates the registry used for local and async domain-event subscriber dispatch.
     */
    @Bean
    @ConditionalOnMissingBean
    public DomainEventSubscriberRegistry domainEventSubscriberRegistry(List<DomainEventSubscriber<?>> subscribers,
                                                                      PlatformMessageSerializer serializer) {
        return new DefaultDomainEventSubscriberRegistry(subscribers, serializer);
    }

    /**
     * Creates the handler that bridges asynchronously delivered domain events back to local subscribers.
     */
    @Bean
    @ConditionalOnMissingBean
    public DomainEventMqHandler domainEventMqHandler(DomainEventSubscriberRegistry registry,
                                                     AsyncPlatformProperties properties) {
        return new DomainEventMqHandler(registry, properties.getDomainEvents().getSubTopic());
    }

    /**
     * Creates the high-level domain-event publisher abstraction.
     */
    @Bean
    @ConditionalOnMissingBean(DomainEventPublisher.class)
    public DomainEventPublisher domainEventPublisher(TaskPublisher taskPublisher,
                                                     DomainEventSubscriberRegistry registry,
                                                     PlatformMessageSerializer serializer,
                                                     AsyncPlatformProperties properties) {
        // Domain events are published through the same task transport, but keep a dedicated API.
        return new DefaultDomainEventPublisher(
                taskPublisher,
                registry,
                serializer,
                properties.getDomainEvents().getTopic(),
                properties.getDomainEvents().getSubTopic(),
                properties.getKafka().getClientId(),
                properties.getDomainEvents().isAllowLocalDispatch()
        );
    }

    /**
     * Creates the runtime handler registry from binding configuration and discovered handlers.
     */
    @Bean
    @ConditionalOnMissingBean
    public HandlerRegistry handlerRegistry(AsyncPlatformProperties properties,
                                           List<SingleMqHandler<?>> handlers) {
        Collection<HandlerBinding> bindings = properties.getConsumer().getBindings().entrySet().stream()
                .map(entry -> toBinding(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        return new HandlerRegistry(bindings, handlers);
    }

    /**
     * Creates the Kafka consumer lifecycle manager that drives polling and dispatch.
     */
    @Bean
    @ConditionalOnMissingBean
    public KafkaConsumerBindingManager kafkaConsumerBindingManager(AsyncPlatformProperties properties,
                                                                  HandlerRegistry handlerRegistry,
                                                                  PlatformMessageSerializer serializer,
                                                                  TaskPublisher taskPublisher) {
        KafkaConsumerOptions options = KafkaConsumerOptions.builder()
                .bootstrapServers(properties.getKafka().getBootstrapServers())
                .clientId(properties.getKafka().getClientId())
                .pollTimeoutMs(properties.getConsumer().getPollTimeoutMs())
                .build();
        return new KafkaConsumerBindingManager(options, handlerRegistry, serializer, taskPublisher);
    }

    /**
     * Converts one external binding entry into the runtime binding model.
     */
    private HandlerBinding toBinding(String name, AsyncPlatformProperties.Binding binding) {
        return HandlerBinding.builder()
                .name(name)
                .enabled(binding.isEnabled())
                .handlerName(binding.getHandlerName())
                .topic(binding.getTopic())
                .group(binding.getGroup())
                .subTopics(binding.getSubTopics())
                .concurrency(binding.getConcurrency() == null ? 1 : binding.getConcurrency())
                .retryTopic(binding.getRetryTopic())
                .dlqTopic(binding.getDlqTopic())
                .poisonTopic(binding.getPoisonTopic())
                .build();
    }
}
