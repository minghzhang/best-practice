# Async Platform 目标与当前实现现状

## 1. 文档目的

这份文档用于沉淀两件事：

- 我们到底想把 `async-platform` 做成什么
- 到目前为止已经实现了哪些内容

它适合作为当前项目快照，方便后续设计评审、继续开发和新同学快速了解项目。

## 2. 项目目标

`async-platform` 的目标，是在 Kafka 之上构建一层可复用的异步消息平台，同时让业务代码不直接依赖 Kafka 原生 API。

我们要做的不是一个简单的 producer / consumer wrapper，而是一套完整的平台编程模型。它应该具备下面这些特征：

- 业务模块依赖平台抽象，而不是直接依赖 `KafkaProducer` / `KafkaConsumer`
- 消费者拓扑通过配置驱动，而不是写死在业务代码里
- Domain Event 是一等概念，可以本地分发，也可以通过 MQ 分发
- retry、DLQ、poison、启动校验等运行时治理由平台负责
- 本地运行、调试和自动化验证足够简单

## 3. 当前 v1 目标范围

当前阶段的 v1 目标，是先建立一个稳定的第一版实现基线，覆盖下面这些核心能力：

- 多模块工程结构
- task 和 domain event 的核心契约
- 基于 Kafka 的消息发送
- 配置驱动的消费者注册
- 基于 `subTopic` 的消息分发
- 本地和 MQ 两种 domain event 分发能力
- 有边界的 retry 和 DLQ 路由
- 启动期 fail-fast 配置校验
- 一个能够展示主要用法的 demo app
- 一组能覆盖主链路的自动化测试

这一版的重点不是“一次把所有能力都做完”，而是先把架构骨架、运行时主链路和验证基线搭起来。

## 4. 目标架构概览

当前项目的目标模块划分如下：

- `async-platform-core`
  定义对业务暴露的核心抽象，包括 task envelope、handler、publish result、domain event 等。
- `async-platform-runtime`
  负责序列化、handler registry、dispatch group 和 domain event runtime。
- `async-platform-kafka-adapter`
  负责把平台抽象映射到 Kafka 的生产和消费运行时。
- `async-platform-spring-boot-starter`
  负责 Spring Boot 自动装配和 typed properties。
- `async-platform-demo-app`
  作为参考应用，展示任务发布、事件发布和消费接入方式。

## 5. 已经实现的内容

### 5.1 工程骨架

`async-platform/` 下已经是完整的 Maven 多模块结构。

已实现：

- 父 `pom.xml`
- 五个子模块
- 本地 Kafka `compose.yml`
- 顶层 `README.md`

这意味着项目已经不是文档阶段，而是一个可编译、可运行、可继续演进的代码基线。

### 5.2 核心契约层

平台已经具备第一版 task / domain event 编程模型。

已实现：

- `TaskEnvelope`
- `TrackingMetadata`
- `RoutingMetadata`
- `DeliveryMetadata`
- `PublishResult`
- `ConsumeResult`
- `ConsumeDisposition`
- `TaskPublisher`
- `SingleMqHandler`
- `DomainEvent`
- `DomainEventEnvelope`
- `DomainEventPublisher`
- `DomainEventSubscriber`

这意味着业务代码已经可以通过平台抽象发布 task 和 domain event，而不是直接依赖 Kafka API。

### 5.3 Runtime 层

runtime 层已经具备第一版平台运行时能力。

已实现：

- 基于 Jackson 的消息序列化
- handler binding 模型
- handler registry
- 按 `topic + group` 构建 dispatch group
- 基于 `subTopic` 的 handler 路由
- 本地 domain event subscriber registry
- 默认 domain event publisher

这意味着消息分发关系已经由平台托管，而不是由业务自己拼装。

### 5.4 Kafka Adapter 层

Kafka 适配层已经具备第一版基于 Kafka 的 transport 实现。

已实现：

- Kafka 版 `TaskPublisher`
- Kafka header mapper
- Kafka consumer lifecycle manager
- 基于配置创建 consumer 并轮询消息
- 从消息记录到 handler 的运行时分发
- retry 路由
- DLQ 路由
- poison 路由 hook
- retry attempt 递增和 retry 上限控制
- 更安全的 consumer shutdown 行为

这意味着平台已经不是“只有发送，没有运行时”，而是具备了完整的异步主链路。

### 5.5 Spring Boot Starter

starter 已经把平台封装成了应用可直接接入的形态。

已实现：

- `AsyncPlatformProperties`
- starter 自动装配
- serializer、registry、publisher、consumer manager 的自动注入
- Kafka 和 binding 关键配置的 fail-fast 启动校验

这意味着一个 Spring Boot 应用接入平台时，不需要自己手工拼装大量基础 bean。

### 5.6 Domain Event 集成

Domain Event 已经不只是设计概念，而是进入了当前基线实现。

已实现：

- 本地 event dispatch
- 基于 MQ 的 event dispatch
- `BOTH` 模式
- starter 中的 domain event bridge handler

这证明了一个关键设计点：Domain Event 是平台级概念，MQ 只是 transport 之一。

### 5.7 Demo App

demo 应用已经能够展示第一版主要用法。

已实现：

- 发布普通异步 task
- 发布“重试一次后成功”的 task
- 发布“直接进入 DLQ”的 task
- 发布 `BOTH` 模式的 domain event
- 通过内存 observation store 观察 handler / subscriber 的处理结果

这让项目已经具备了很清晰的示例入口。

### 5.8 本地运行支持

已实现：

- 单节点 Kafka compose
- 本地启动说明
- README 中的手工验证命令

这足够支撑当前 v1 基线的本地演示和开发验证。

## 6. 当前验证状态

当前基线不只是“代码写出来了”，而且已经做过验证。

已验证：

- starter 配置校验测试已通过
- demo integration test 已通过
- 当前已经覆盖的主要链路包括：
  - 正常 task 消费
  - domain event 的本地 + MQ 双路径分发
  - retry 一次后成功
  - retry 预算耗尽后进入 DLQ
  - 显式返回 DLQ 的处理路径

当前测试快照：

- `AsyncPlatformAutoConfigurationTest`：3 个测试通过
- `AsyncPlatformDemoIntegrationTest`：5 个测试通过

## 7. 当前已经具备的功能基线

截至目前，项目已经能完整演示下面这条端到端主链路：

- 应用通过 `TaskPublisher` 发布 task
- task 被序列化并发送到 Kafka
- 配置驱动的 consumer 收到消息
- 平台按 `subTopic` 解析目标 handler
- handler 返回 `SUCCESS`、`RETRY`、`DLQ`、`POISON` 或 `SKIP`
- 平台根据结果执行对应路由
- domain event 可以选择本地分发、异步分发或两者同时进行

这已经是一条真正可运行的第一版垂直切片。

## 8. 当前还没有实现的部分

下面这些能力仍然没有进入当前基线，属于下一阶段内容：

- 专门的 poison message model 和 replay 工具
- delay scheduler
- outbox relay
- idempotency / dedupe 机制
- 更接近生产可用的 replay 与运维能力
- 更复杂的 routing / 多集群支持
- 动态配置刷新
- 更完善的 metrics、audit 和 observability
- 更严格的 schema evolution 策略

这些不是缺陷回归，而是当前 v1 刻意没有展开的后续能力层。

## 9. 推荐阅读顺序

如果有人想快速理解当前实现，建议按下面顺序阅读：

1. `README.md`
2. `async-platform-core`
3. `async-platform-runtime`
4. `async-platform-kafka-adapter`
5. `async-platform-spring-boot-starter`
6. `async-platform-demo-app`

## 10. 总结

`async-platform` 目前已经从“设计方案”进入“真实代码落地”阶段。

当前已经具备：

- 一个清晰的多模块实现骨架
- 面向业务的 task / domain event 编程模型
- Kafka-backed 的运行时主链路
- 配置驱动的消费者注册机制
- domain event 集成
- 有边界的 retry / DLQ 行为
- 可运行的 demo app
- 能覆盖主链路的测试基线

也就是说，项目已经具备继续向下一阶段推进的良好基础。
