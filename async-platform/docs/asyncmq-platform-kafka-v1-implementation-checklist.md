# AsyncMQ Platform on Kafka V1 Implementation Checklist

## Document Control

| Field | Value |
| --- | --- |
| Status | Draft |
| Based On | `docs/asyncmq-platform-kafka-technical-design.md` |
| Last Updated | 2026-04-15 |
| Goal | Turn the technical design into an implementation-ready v1 checklist |

---

## 1. Purpose

This checklist translates the technical design into an execution plan for v1 delivery. It is intended to help engineering teams break the platform into concrete workstreams, implementation tasks, validation gates, and rollout steps.

This document is not a replacement for the technical design. It is the delivery companion for that design.

---

## 2. V1 Scope

### 2.1 In Scope for V1

- `mq-core-api`
- `mq-kafka-adapter`
- `mq-runtime`
- `mq-spring-starter`
- configuration-driven consumer registration
- subTopic-based dispatch
- consume pipeline with context recovery and cleanup
- retry, DLQ, and poison routing
- core metrics and audit logs
- `domain-event-core`
- `domain-event-mq-bridge`
- one reference producer and consumer flow
- one reference domain event flow

### 2.2 Explicitly Out of Scope for V1

- delay scheduler
- fixed-time task scheduler
- outbox relay
- advanced cluster-aware routing beyond extension points
- dynamic hot reload of all consumer bindings
- schema registry adoption
- multi-language SDK
- Kafka transaction-based general solution

### 2.3 V1 Exit Criteria

- A business service can publish a task without importing Kafka APIs.
- A business service can consume a task using a `SingleMqHandler` style contract.
- Consumer binding is driven by configuration, not only by code.
- The platform supports retry, DLQ, and poison handling.
- The platform supports local and MQ-backed domain event delivery.
- The platform has a production-ready baseline for metrics, audit, and operational controls.

---

## 3. Delivery Strategy

### 3.1 Recommended Milestones

| Milestone | Outcome |
| --- | --- |
| M1 | Foundation modules and contracts are in place |
| M2 | Kafka-backed async task publishing and consumption works end-to-end |
| M3 | Runtime hardening is complete with retry, DLQ, metrics, and validation |
| M4 | Domain event integration is complete |
| M5 | Reference adoption, documentation, and rollout gates are complete |

### 3.2 Recommended Workstream Order

1. foundation and module scaffolding
2. core message and handler contracts
3. Kafka adapter
4. Spring starter and configuration binding
5. consumer registry and dispatch
6. consume pipeline and failure routing
7. observability and governance
8. domain event integration
9. reference service adoption
10. rollout readiness

---

## 4. Workstream A: Repo and Module Scaffolding

### Checklist

- [ ] Create the target modules declared in the TDS.
- [ ] Define clear package boundaries for API, runtime, adapter, and domain-event layers.
- [ ] Add build configuration and dependency management for each module.
- [ ] Ensure only `mq-kafka-adapter` depends on Kafka clients.
- [ ] Add dependency checks or static rules to prevent Kafka imports outside the adapter.
- [ ] Create a minimal README for each module describing its purpose.
- [ ] Define the initial artifact names and ownership expectations.

### Exit Criteria

- The project builds successfully with all platform modules present.
- Dependency boundaries are enforced by build or static analysis.
- Module ownership and purpose are documented.

---

## 5. Workstream B: Core API Contracts

### Checklist

- [ ] Define `TaskEnvelope` as the standard task transport model.
- [ ] Define `TrackingMetadata`, `RoutingMetadata`, and `DeliveryMetadata`.
- [ ] Define the `TaskPublisher` contract.
- [ ] Define the `SingleMqHandler<T>` style consumer contract.
- [ ] Define standard consume result and publish result models.
- [ ] Define error classification interfaces for retryable vs non-retryable failures.
- [ ] Define extension points for serialization and payload type mapping.
- [ ] Define schema version handling for envelopes.
- [ ] Define contract documentation for task id, trace id, subTopic, key, and dedupe semantics.

### Exit Criteria

- Core APIs are stable enough for runtime and adapter implementation.
- No Kafka-specific concepts leak into business-facing APIs.
- Envelope fields are documented with compatibility expectations.

---

## 6. Workstream C: Kafka Adapter

### Checklist

- [ ] Implement Kafka producer factory and lifecycle management.
- [ ] Implement Kafka consumer factory and lifecycle management.
- [ ] Implement record key mapping from platform routing metadata.
- [ ] Implement header mapping for trace id, task id, subTopic, payload type, and schema version.
- [ ] Implement value serialization and deserialization SPI integration.
- [ ] Implement sensible producer defaults for acks, retries, batching, and compression.
- [ ] Implement consumer polling and manual commit support.
- [ ] Define adapter-level error handling for malformed records.
- [ ] Add adapter integration tests against a real Kafka test environment.

### Exit Criteria

- Producer and consumer communication works end-to-end against Kafka.
- Platform headers and payloads can round-trip correctly.
- Adapter tests prove key routing, header mapping, and deserialization behavior.

---

## 7. Workstream D: Spring Starter and Configuration Binding

### Checklist

- [ ] Create configuration properties for connection, producer, consumer bindings, retry, and domain event delivery.
- [ ] Add Spring auto-configuration for platform beans.
- [ ] Add conditional bean loading for optional components.
- [ ] Bind consumer topology configuration into typed classes.
- [ ] Add startup validation for missing topic, group, subTopic, and invalid consume mode.
- [ ] Add a health indicator or startup report for loaded bindings.
- [ ] Document the expected config shape for adopters.

### Exit Criteria

- A Spring Boot service can enable the platform with configuration only.
- Misconfigured bindings fail early and clearly.
- Loaded bindings are visible at startup.

---

## 8. Workstream E: Consumer Registry and SubTopic Dispatch

### Checklist

- [ ] Implement scanning for all `SingleMqHandler` beans.
- [ ] Match handlers with configuration-defined bindings.
- [ ] Build a dispatch registry for `topic + group + subTopic -> handler`.
- [ ] Support one binding subscribing to one or more topics.
- [ ] Support broadcast mode by generating instance-unique or scope-unique groups.
- [ ] Support sequential mode requirements by validating key usage.
- [ ] Add duplicate binding conflict detection.
- [ ] Add startup logs summarizing registered handlers and bindings.

### Exit Criteria

- A configured handler is automatically registered and started at boot.
- Incoming messages are dispatched to handlers by subTopic.
- Conflicting bindings are detected before traffic begins.

---

## 9. Workstream F: Consume Pipeline

### Checklist

- [ ] Implement deserialization and schema validation stage.
- [ ] Implement trace and request context recovery.
- [ ] Implement routing filter stage.
- [ ] Implement idempotency extension hook.
- [ ] Implement business handler invocation stage.
- [ ] Implement result classification into success, retry, DLQ, or poison.
- [ ] Implement final cleanup stage for thread-local and execution context.
- [ ] Define pipeline ordering clearly and keep it stable.
- [ ] Add tests covering success, handler failure, malformed message, and cleanup behavior.

### Exit Criteria

- Every consumed message passes through one standard pipeline.
- Context is always cleaned up even on exceptions.
- The pipeline produces deterministic routing and failure behavior.

---

## 10. Workstream G: Retry, DLQ, and Poison Handling

### Checklist

- [ ] Define a retry classifier interface.
- [ ] Implement default retry policy resolution from configuration.
- [ ] Implement attempt counting and attempt propagation in message metadata.
- [ ] Implement retry publishing to retry topics.
- [ ] Implement DLQ publishing with original message context preserved.
- [ ] Implement poison topic handling for malformed envelopes and deserialization failures.
- [ ] Preserve original topic, partition, offset, exception type, and attempt in DLQ metadata.
- [ ] Add operational guidance for replaying DLQ messages safely.
- [ ] Add tests covering retry exhaustion and DLQ routing.

### Exit Criteria

- Retryable and non-retryable failures are distinguishable.
- DLQ records contain enough information for investigation and replay.
- Poison messages cannot repeatedly break consumers.

---

## 11. Workstream H: Observability and Audit

### Checklist

- [ ] Emit producer success and failure counters.
- [ ] Emit consumer throughput, latency, retry, and DLQ metrics.
- [ ] Emit consumer lag metrics or hooks to an existing lag monitor.
- [ ] Add structured audit logs for publish and consume lifecycle.
- [ ] Include topic, subTopic, group, taskId, traceId, handler, attempt, and result in logs.
- [ ] Add redaction support for sensitive payload fields.
- [ ] Prepare dashboards for producer health, consumer health, retry volume, and DLQ volume.
- [ ] Prepare alert recommendations for lag spikes, DLQ spikes, and retry storms.

### Exit Criteria

- Operators can observe platform health without custom service-specific instrumentation.
- Logs and metrics are sufficient to debug a failing binding.

---

## 12. Workstream I: Domain Event Core

### Checklist

- [ ] Define the `DomainEvent` base contract.
- [ ] Define event naming and event identity conventions.
- [ ] Define `DomainEventPublisher`.
- [ ] Define `DomainEventSubscriber`.
- [ ] Implement a local subscriber registry.
- [ ] Implement delivery mode selection for `local`, `async`, and `both`.
- [ ] Define compatibility guidance for event version evolution.
- [ ] Document rules for when to use tasks vs domain events.

### Exit Criteria

- Domain events can be published and consumed locally without MQ.
- Business services publish through one stable domain event API.

---

## 13. Workstream J: Domain Event MQ Bridge

### Checklist

- [ ] Define `DomainEventEnvelope`.
- [ ] Implement outbound conversion from domain event to MQ envelope.
- [ ] Implement inbound conversion from MQ envelope back to domain event.
- [ ] Implement event topic resolution and delivery policy integration.
- [ ] Implement support for `allowOtherGroupConsume`.
- [ ] Implement support for target cluster or target scope metadata as extension points.
- [ ] Ensure the inbound bridge dispatches to the local subscriber registry.
- [ ] Add tests for `local`, `async`, and `both` delivery behavior.
- [ ] Add tests for event compatibility and unknown event fields.

### Exit Criteria

- Domain events can flow over MQ without business code referencing Kafka or topic details.
- Local and async delivery modes are interchangeable from the publisher perspective.

---

## 14. Workstream K: Reference Adoption

### Checklist

- [ ] Build one reference producer example using the new task API.
- [ ] Build one reference consumer example using config-driven binding.
- [ ] Build one reference domain event publisher and subscriber flow.
- [ ] Include one idempotent mutation handler example.
- [ ] Include one retryable failure example and one DLQ example.
- [ ] Document the reference flow end-to-end.
- [ ] Capture sample config for adopters.

### Exit Criteria

- The platform has at least one real or demo workflow proving the happy path and failure path.
- New teams can copy the reference flow to onboard quickly.

---

## 15. Workstream L: Developer Experience and Documentation

### Checklist

- [ ] Document the module purpose and dependency boundaries.
- [ ] Document how to publish a task.
- [ ] Document how to implement a handler.
- [ ] Document how to bind a handler via configuration.
- [ ] Document retry and DLQ expectations.
- [ ] Document when to choose task vs domain event.
- [ ] Document local development and test setup with Kafka.
- [ ] Create an adoption guide for the first service teams.

### Exit Criteria

- A new engineer can onboard to the platform without reading internal implementation details first.
- The first adopting team does not need platform engineers for every integration step.

---

## 16. Workstream M: Test Strategy

### Checklist

- [ ] Add unit tests for envelope creation and validation.
- [ ] Add unit tests for serialization and deserialization.
- [ ] Add unit tests for dispatch registry behavior.
- [ ] Add unit tests for retry classification and DLQ routing.
- [ ] Add unit tests for context recovery and cleanup.
- [ ] Add unit tests for domain event delivery strategies.
- [ ] Add integration tests with Kafka for producer and consumer round-trip.
- [ ] Add startup validation tests for bad bindings.
- [ ] Add one load or soak test for a representative binding.
- [ ] Add one failure injection test for retry and DLQ flow.

### Exit Criteria

- The test suite covers core contracts, runtime flow, and failure behavior.
- A regression in envelope compatibility or dispatch wiring is caught before release.

---

## 17. Workstream N: Rollout Readiness

### Checklist

- [ ] Identify the first adopting service and workflow.
- [ ] Confirm topic ownership and naming approval.
- [ ] Prepare production configuration and secrets management.
- [ ] Prepare dashboards and alert thresholds before rollout.
- [ ] Prepare a rollback switch or disable switch for the first binding.
- [ ] Define success metrics for the first rollout.
- [ ] Run a pre-production test with synthetic traffic.
- [ ] Run a canary or limited rollout.
- [ ] Confirm on-call ownership and incident path.
- [ ] Publish a runbook for pause, drain, retry, and DLQ replay operations.

### Exit Criteria

- The team can safely enable or disable the first production binding.
- Operators have enough runbook coverage to handle expected failure modes.

---

## 18. Recommended Epic Breakdown

| Epic | Suggested Scope |
| --- | --- |
| E1 | Module scaffolding and core API |
| E2 | Kafka adapter and serialization |
| E3 | Spring starter and config binding |
| E4 | Consumer registry and subTopic dispatch |
| E5 | Consume pipeline and cleanup |
| E6 | Retry, DLQ, poison handling |
| E7 | Metrics, audit, and dashboards |
| E8 | Domain event core |
| E9 | Domain event MQ bridge |
| E10 | Reference flow and rollout |

---

## 19. Definition of Done for V1

V1 is done only when all of the following are true:

- [ ] The core platform modules are merged and versioned.
- [ ] Kafka-based task publish and consume is working end-to-end.
- [ ] Consumer registration is configuration-driven and validated at startup.
- [ ] Retry, DLQ, and poison behavior are implemented and tested.
- [ ] The consume pipeline handles context recovery and cleanup consistently.
- [ ] Domain event local and MQ-backed delivery both work.
- [ ] At least one reference adoption flow is documented and verified.
- [ ] Dashboards, alerts, and runbooks are ready before production rollout.
- [ ] The first service rollout passes canary validation with defined success metrics.

---

## 20. Suggested First Two Sprints

### Sprint 1

- [ ] Create platform modules and dependency boundaries.
- [ ] Define task envelope and handler contracts.
- [ ] Implement basic Kafka producer and consumer adapter.
- [ ] Implement Spring starter and basic config binding.
- [ ] Prove one round-trip producer to consumer demo.

### Sprint 2

- [ ] Implement consumer registry and subTopic dispatch.
- [ ] Implement consume pipeline with cleanup.
- [ ] Implement retry, DLQ, and poison routing.
- [ ] Add producer and consumer metrics and structured audit logs.
- [ ] Add one reference task flow and one domain event proof-of-concept.

---

## 21. Suggested V1 Review Gates

| Gate | Required Evidence |
| --- | --- |
| Design gate | TDS approved and checklist agreed |
| Foundation gate | Modules, boundaries, and contracts merged |
| Runtime gate | End-to-end task flow and failure routing verified |
| Event gate | Domain event local and MQ modes verified |
| Rollout gate | Dashboards, alerts, runbook, canary plan approved |

---

## 22. Notes for Task Breakdown

- Prefer one owner per workstream, even if implementation is shared.
- Keep write ownership clear between API, runtime, adapter, and event layers.
- Do not let the first adopter define the platform contract alone.
- Keep v1 narrow. Delay scheduler and outbox should remain post-v1 unless they become blockers.
- Treat operational readiness as a delivery item, not a follow-up item.

