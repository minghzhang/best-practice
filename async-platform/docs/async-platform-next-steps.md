# Async Platform 下一步工作计划

## 1. 文档目的

这份文档用于说明在当前 v1 基线之上，接下来最合理的实现顺序是什么、为什么这么排，以及每一阶段应该交付什么。

它关注的是“下一步怎么做”，而不是重复当前已经完成的内容。

## 2. 当前出发点

我们已经不是从零开始。

当前基线已经具备：

- task / domain event 的核心抽象
- Kafka-backed 的发送和消费主链路
- 配置驱动的 handler 注册
- 本地和 MQ 两种 domain event 分发
- 有上限的 retry 和 DLQ 路由
- fail-fast 启动配置校验
- demo app 和 integration test

因此下一阶段的重点，不应该是重写骨架，而应该是继续加强运行时可靠性和平台可用性。

## 3. 推荐的下一阶段优先级

### 优先级 1：Poison Message 处理与 Replay

这是最值得优先做的下一项。

原因：

- 当前代码已经有 poison 路由 hook，但能力还比较轻
- 序列化失败、不可恢复 payload 错误等场景，需要更完整的承接方式
- 一个异步平台如果没有 poison inspect / replay 方案，后续接入业务会比较危险

建议实现内容：

- 定义专门的 poison message model
- 记录原始 topic、partition、offset、失败原因和原始 payload
- 明确 poison topic 或 poison storage 的承载方式
- 增加 poison replay 入口
- 增加反序列化失败和 poison routing 的测试

期望结果：

- poison failure 从“有 hook 但不可运维”，提升到“有模型、有落点、可回放”

### 优先级 2：Idempotency 与重复投递保护

这是下一层运行时安全基线。

原因：

- 当前平台本质上仍然是 at-least-once 语义
- retry、重放、重复消费都可能导致业务重复执行
- 如果没有平台级幂等抽象，后面功能越多，接入成本越高

建议实现内容：

- 明确基于 `taskId` 或 `dedupeKey` 的幂等契约
- 定义 idempotency store 抽象
- 先提供一版内存实现，用于 demo 和测试
- 设计 Redis 或 DB 的后续实现接口
- 增加 duplicate delivery 测试

期望结果：

- 平台开始具备重复投递防护能力，而不是把这个问题完全丢给业务

### 优先级 3：Outbox 集成

这是平台走向真实业务一致性的关键一步。

原因：

- 真实业务里经常有“数据库更新 + 发 MQ”的一致性问题
- 当前直接 publish 足够做 demo，但不够支撑事务型业务接入
- Outbox 是更适合作为第一版一致性方案的路径

建议实现内容：

- 定义 outbox record model
- 定义 outbox publisher / relay contract
- 实现一版简单 polling relay
- 增加示例链路：业务写库 -> 写 outbox -> relay 发 MQ
- 增加 outbox 相关测试

期望结果：

- 平台可以开始承接更真实的业务写入与异步发布场景

### 优先级 4：Delay Message 支持

这项建议放在可靠性基线之后。

原因：

- Kafka 本身不适合直接表达业务常说的 delay 语义
- 很多异步工作流都需要“到点再投递”
- 这是一类高频需求，但应建立在前面的可靠性能力之上

建议实现内容：

- 定义 delay task model
- 选定第一版 delay scheduler 实现方式
- 支持 `notBefore` 语义
- 到期后重新投递到正常 topic
- 增加 delay 行为测试

期望结果：

- 平台能够覆盖一类典型的延迟异步场景

### 优先级 5：Metrics、Audit 与运维可观测性

在 runtime 路径变复杂之前，运维能力要同步补上。

原因：

- 后续一旦引入 poison、outbox、delay，问题定位复杂度会明显上升
- 平台越通用，越需要清晰的运行时可观测性

建议实现内容：

- 增加结构化 publish / consume 日志
- 增加 publish success、consume success、retry count、DLQ count、poison count 等指标
- 增加比 demo observation store 更正式的状态暴露方式
- 补一版 troubleshooting 和 replay runbook

期望结果：

- 平台变得更容易排查和更接近真实运维需求

## 4. 推荐的实现顺序

推荐顺序如下：

1. poison handling 和 replay
2. idempotency
3. outbox integration
4. delay scheduling
5. observability 和 operations hardening

这样排序的原因是：

- 先补运行时失败治理
- 再补重复投递安全性
- 再补事务一致性
- 最后再扩展更多异步特性和运维能力

这是“先把底座补稳，再往上加能力”的路线。

## 5. 建议的阶段拆分

### 阶段 A：运行时失败治理增强

目标：

- 让失败路径变得清晰、可追踪、可回放

范围：

- poison message model
- poison routing 实现
- replay 入口
- idempotency 抽象
- duplicate delivery 测试

完成标准：

- 反序列化失败有确定处理路径
- poison 消息有落点、有原因、有重放入口
- duplicate delivery 有平台级抽象和测试覆盖

### 阶段 B：一致性能力

目标：

- 让平台可承接更真实的事务型业务场景

范围：

- outbox record contract
- relay process
- 示例应用接入
- outbox 测试

完成标准：

- 业务写入和异步发布能通过 outbox 串起来
- outbox 主链路在 demo 或 sample 中可验证

### 阶段 C：异步能力扩展

目标：

- 扩大平台支持的异步模式

范围：

- delay task
- scheduler 实现
- 定时释放测试

完成标准：

- 平台支持 delay 任务发布和到期重投递

### 阶段 D：运维与可观测性加强

目标：

- 让平台更适合长期维护和问题定位

范围：

- metrics
- audit log
- runbook
- replay 指导文档

完成标准：

- 不看底层 runtime 代码，也能理解主要故障路径和运维手段

## 6. 最实用的下一批交付物

如果我们希望保持实现节奏，最值得下一轮直接交付的是：

- `poison-message-design.md`
- poison route 真实实现
- poison replay demo API 或命令入口
- idempotency abstraction
- 一版内存型 idempotency store
- poison / duplicate delivery 的 integration tests

这是性价比最高、也最能继续稳住平台底座的一批工作。

## 7. 需要特别注意的风险

下一阶段需要留意下面这些风险：

- 一次塞入太多功能，导致每层都不完整
- poison 数据模型还没定义清楚就先做 replay
- 在更多 delivery mode 进入之前，没有先补幂等能力
- runtime 复杂度增长速度超过测试和可观测性的增长速度

更稳妥的做法仍然是按层推进，每加一层都要有对应测试和文档。

## 8. 下一轮完成标准建议

下一轮工作建议至少满足下面这些条件才算完成：

- poison routing 已经真正实现
- replay 行为有明确约定和可运行入口
- duplicate delivery 有平台级抽象
- integration tests 覆盖新增运行时路径
- README 或 docs 同步更新新的行为说明

## 9. 总结

当前基线已经足够好，可以放心进入下一阶段。

最合理的下一步不是“继续横向铺功能”，而是优先把运行时可靠性这条线做深：

- 先把 poison 做实
- 再把 idempotency 做起来
- 然后补 outbox 和 delay
- 最后再继续做更完整的 observability 和 operations

这条路线风险最低，也最符合一个平台型项目的成长顺序。
