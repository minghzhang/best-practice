# Async Platform

This directory contains the first implementation slice of the async messaging platform described in the TDS and detailed design docs.

## Modules

- `async-platform-core`: core contracts exposed to business code, including tasks, handlers, and domain events
- `async-platform-runtime`: transport-agnostic runtime services such as serialization, registry, and domain-event dispatch
- `async-platform-kafka-adapter`: Kafka-specific publisher and consumer runtime integration
- `async-platform-spring-boot-starter`: Spring Boot configuration, validation, and auto-wiring entry point
- `async-platform-demo-app`: reference application and integration-test carrier showing how to use the platform

For a Chinese module-by-module explanation, see [docs/async-platform-module-responsibilities.md](./docs/async-platform-module-responsibilities.md).

## What Works Right Now

- task publishing through `TaskPublisher`
- configuration-driven consumer registration
- fail-fast startup validation for Kafka and binding configuration
- subTopic based dispatch to `SingleMqHandler`
- local and MQ-backed domain event dispatch
- bounded retry / DLQ / poison topic routing hooks
- demo app with REST endpoints and an in-memory observation store

## Start Local Kafka

```bash
cd async-platform
docker compose up -d kafka
```

Kafka will be exposed at `localhost:9092`.

## Run the Demo App

```bash
cd async-platform
mvn -pl async-platform-demo-app spring-boot:run
```

The demo app starts on `http://localhost:8088`.

## Manual Validation

Publish an async task:

```bash
curl -X POST http://localhost:8088/api/demo/tasks/orders/order-1001
```

Publish a task that retries once and then succeeds from the retry topic:

```bash
curl -X POST http://localhost:8088/api/demo/tasks/retry/orders/order-1002
```

Publish a task that is routed directly to the DLQ:

```bash
curl -X POST http://localhost:8088/api/demo/tasks/dlq/orders/order-1003
```

Publish a domain event in `BOTH` mode:

```bash
curl -X POST http://localhost:8088/api/demo/events/orders/order-2001
```

Inspect what the handlers and subscribers observed:

```bash
curl http://localhost:8088/api/demo/observations
```

## Automated Validation

Run the full module build including the embedded Kafka integration test:

```bash
cd async-platform
mvn test
```

## Current Gaps

- no delay scheduler yet
- no outbox relay yet
- no production-grade replay tooling yet
- routing is intentionally simple in v1 and ready for extension
