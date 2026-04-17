# Async Platform Module Responsibilities

## 1. 文档目的

这份文档专门说明 `async-platform` 里每个 module 的职责边界，方便阅读代码时先建立整体地图。

## 2. 模块总览

当前项目包含 5 个核心 module：

- `async-platform-core`
- `async-platform-runtime`
- `async-platform-kafka-adapter`
- `async-platform-spring-boot-starter`
- `async-platform-demo-app`

它们之间的关系不是“谁都能随便依赖谁”，而是一个分层结构。

## 3. 各个 Module 的作用

### `async-platform-core`

这是平台最底层的抽象层。

职责：

- 定义 task、event、handler、publish result、consume result 等核心契约
- 提供对业务暴露的统一编程模型
- 不依赖 Kafka
- 不依赖 Spring Boot 自动装配

你可以把它理解为：

- “平台语言层”
- “业务和运行时之间的公共契约层”

典型内容：

- `TaskEnvelope`
- `TaskPublisher`
- `SingleMqHandler`
- `DomainEvent`
- `DomainEventPublisher`

### `async-platform-runtime`

这是平台的通用运行时层。

职责：

- 把 `core` 里的抽象真正组织成可运行的 dispatch 逻辑
- 负责序列化、handler registry、dispatch group、subscriber registry
- 提供 domain event 的本地分发和 envelope 转换逻辑
- 仍然不直接绑定 Kafka transport 细节

你可以把它理解为：

- “平台中间层”
- “把抽象变成运行时行为的地方”

典型内容：

- `HandlerRegistry`
- `DispatchGroup`
- `JacksonPlatformMessageSerializer`
- `DefaultDomainEventPublisher`
- `DefaultDomainEventSubscriberRegistry`

### `async-platform-kafka-adapter`

这是 Kafka transport 适配层。

职责：

- 使用 `kafka-clients` 实现发送和消费
- 把 Kafka record 映射到平台 envelope
- 承载 poll、commit、retry、DLQ、poison 等与 Kafka 消费循环强相关的逻辑
- 保证上层模块不必直接依赖 Kafka API

你可以把它理解为：

- “平台和 Kafka 之间的连接层”
- “transport adapter”

典型内容：

- `KafkaTaskPublisher`
- `KafkaConsumerBindingManager`
- `KafkaHeaderMapper`
- `KafkaProducerOptions`
- `KafkaConsumerOptions`

### `async-platform-spring-boot-starter`

这是 Spring Boot 接入层。

职责：

- 提供 `@ConfigurationProperties`
- 自动装配平台需要的核心 bean
- 做启动期 fail-fast 校验
- 把 runtime 和 Kafka adapter 组合成可直接接入应用的 starter

你可以把它理解为：

- “应用接入入口”
- “平台对 Spring Boot 应用的装配层”

典型内容：

- `AsyncPlatformProperties`
- `AsyncPlatformAutoConfiguration`
- `AsyncPlatformConfigurationValidator`
- `DomainEventMqHandler`

### `async-platform-demo-app`

这是示例应用层。

职责：

- 演示业务方如何使用平台
- 提供最小可运行示例
- 作为 integration test 的承载工程
- 验证 task、retry、DLQ、domain event 等主链路

你可以把它理解为：

- “参考接入样板”
- “回归验证载体”

典型内容：

- `DemoController`
- `OrderCreatedHandler`
- `RetryingOrderHandler`
- `DeadLetterObservationHandler`
- `InMemoryDemoObservationStore`

## 4. 推荐的理解顺序

如果要读代码，建议按这个顺序：

1. `async-platform-core`
2. `async-platform-runtime`
3. `async-platform-kafka-adapter`
4. `async-platform-spring-boot-starter`
5. `async-platform-demo-app`

原因是：

- 先理解抽象
- 再理解运行时组织方式
- 再理解 Kafka 是怎么接进来的
- 再理解应用是怎么用起来的

## 5. 依赖关系建议

推荐遵守下面这个依赖方向：

- `demo-app` -> `spring-boot-starter`
- `spring-boot-starter` -> `runtime` + `kafka-adapter` + `core`
- `kafka-adapter` -> `runtime` + `core`
- `runtime` -> `core`
- `core` -> 不依赖上层模块

这个方向能保证平台边界清晰，不会让 Kafka 或 Spring 的细节反向污染核心抽象层。

## 6. 总结

简单说：

- `core` 负责“定义语言”
- `runtime` 负责“组织运行时”
- `kafka-adapter` 负责“接 Kafka”
- `spring-boot-starter` 负责“给应用装配起来”
- `demo-app` 负责“展示怎么用、验证有没有跑通”

理解了这张模块地图，再回头读具体类，会轻松很多。
