package com.bestpractice.asyncplatform.starter;

import static org.assertj.core.api.Assertions.assertThat;

import com.bestpractice.asyncplatform.core.contract.SingleMqHandler;
import com.bestpractice.asyncplatform.core.contract.TaskPublisher;
import com.bestpractice.asyncplatform.core.model.ConsumeResult;
import com.bestpractice.asyncplatform.core.model.PublishResult;
import com.bestpractice.asyncplatform.core.model.TaskEnvelope;
import com.bestpractice.asyncplatform.kafka.KafkaConsumerBindingManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class AsyncPlatformAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class, AsyncPlatformAutoConfiguration.class))
            .withUserConfiguration(TestSupportConfiguration.class);

    @Test
    void shouldFailWhenBootstrapServersMissing() {
        contextRunner.run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure())
                    .hasMessageContaining("async-platform.kafka.bootstrap-servers must not be blank");
        });
    }

    @Test
    void shouldFailWhenBindingHandlerNameMissing() {
        contextRunner
                .withPropertyValues(
                        "async-platform.kafka.bootstrap-servers=localhost:9092",
                        "async-platform.consumer.bindings.invalid.topic=async_platform_tasks",
                        "async-platform.consumer.bindings.invalid.group=test-group",
                        "async-platform.consumer.bindings.invalid.sub-topics[0]=order.created"
                )
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasMessageContaining("async-platform.consumer.bindings.invalid.handler-name must not be blank");
                });
    }

    @Test
    void shouldLoadWithValidMinimumConfiguration() {
        contextRunner
                .withPropertyValues(
                        "async-platform.kafka.bootstrap-servers=localhost:9092",
                        "async-platform.consumer.bindings.order-created.handler-name=testOrderHandler",
                        "async-platform.consumer.bindings.order-created.topic=async_platform_tasks",
                        "async-platform.consumer.bindings.order-created.group=test-group",
                        "async-platform.consumer.bindings.order-created.sub-topics[0]=order.created"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(AsyncPlatformConfigurationValidator.class);
                    assertThat(context).hasSingleBean(TaskPublisher.class);
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class TestSupportConfiguration {
        @Bean
        TaskPublisher taskPublisher() {
            return envelope -> PublishResult.success(envelope.getTaskId(), envelope.getTopic());
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        KafkaConsumerBindingManager kafkaConsumerBindingManager() {
            return new KafkaConsumerBindingManager(null, null, null, null) {
                @Override
                public void start() {
                }

                @Override
                public void stop() {
                }

                @Override
                public boolean isRunning() {
                    return false;
                }
            };
        }

        @Bean
        SingleMqHandler<String> testOrderHandler() {
            return new SingleMqHandler<>() {
                @Override
                public String handlerName() {
                    return "testOrderHandler";
                }

                @Override
                public TypeReference<String> payloadType() {
                    return new TypeReference<>() {};
                }

                @Override
                public ConsumeResult handle(TaskEnvelope<String> envelope) {
                    return ConsumeResult.success();
                }
            };
        }
    }
}
