package com.bestpractice.asyncplatform.demo.task;

import java.time.Instant;

/**
 * Demo payload used by task-based order flows.
 *
 * @param orderId business order identifier
 * @param sku product identifier associated with the order
 * @param createdAt task creation timestamp
 */
public record OrderCreatedPayload(String orderId, String sku, Instant createdAt) {
}
