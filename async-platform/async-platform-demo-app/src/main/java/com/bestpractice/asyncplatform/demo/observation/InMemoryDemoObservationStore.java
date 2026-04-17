package com.bestpractice.asyncplatform.demo.observation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
/**
 * In-memory store used by the demo endpoints and integration tests to observe runtime behavior.
 */
public class InMemoryDemoObservationStore {
    /**
     * Order identifiers handled successfully by the normal task path.
     */
    private final List<String> handledTaskOrderIds = new CopyOnWriteArrayList<>();
    /**
     * Order identifiers that eventually completed through the retry path.
     */
    private final List<String> retriedTaskOrderIds = new CopyOnWriteArrayList<>();
    /**
     * Order identifiers observed on the dead-letter path.
     */
    private final List<String> deadLetterTaskOrderIds = new CopyOnWriteArrayList<>();
    /**
     * Number of domain events observed per order.
     */
    private final Map<String, AtomicInteger> domainEventCounts = new ConcurrentHashMap<>();
    /**
     * Number of retry attempts observed per order.
     */
    private final Map<String, AtomicInteger> retryAttempts = new ConcurrentHashMap<>();

    /**
     * Records that the normal task path handled the given order.
     */
    public void recordTask(String orderId) {
        handledTaskOrderIds.add(orderId);
    }

    /**
     * Records that the retry path eventually handled the given order successfully.
     */
    public void recordRetriedTask(String orderId) {
        retriedTaskOrderIds.add(orderId);
    }

    /**
     * Records that the DLQ path observed the given order.
     */
    public void recordDeadLetterTask(String orderId) {
        deadLetterTaskOrderIds.add(orderId);
    }

    /**
     * Increments the retry attempt count for the given order.
     */
    public void recordRetryAttempt(String orderId) {
        retryAttempts.computeIfAbsent(orderId, ignored -> new AtomicInteger()).incrementAndGet();
    }

    /**
     * Increments the observed domain-event count for the given order.
     */
    public void recordDomainEvent(String orderId) {
        domainEventCounts.computeIfAbsent(orderId, ignored -> new AtomicInteger()).incrementAndGet();
    }

    /**
     * Returns a snapshot of the currently observed demo state.
     */
    public Map<String, Object> snapshot() {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("handledTaskOrderIds", new ArrayList<>(handledTaskOrderIds));
        snapshot.put("retriedTaskOrderIds", new ArrayList<>(retriedTaskOrderIds));
        snapshot.put("deadLetterTaskOrderIds", new ArrayList<>(deadLetterTaskOrderIds));
        Map<String, Integer> eventSnapshot = new LinkedHashMap<>();
        domainEventCounts.forEach((key, value) -> eventSnapshot.put(key, value.get()));
        snapshot.put("domainEventCounts", eventSnapshot);
        Map<String, Integer> retrySnapshot = new LinkedHashMap<>();
        retryAttempts.forEach((key, value) -> retrySnapshot.put(key, value.get()));
        snapshot.put("retryAttempts", retrySnapshot);
        return snapshot;
    }

    /**
     * Returns whether the normal task path has handled the given order.
     */
    public boolean hasHandledTask(String orderId) {
        return handledTaskOrderIds.contains(orderId);
    }

    /**
     * Returns how many domain events have been observed for the given order.
     */
    public int domainEventCount(String orderId) {
        AtomicInteger counter = domainEventCounts.get(orderId);
        return counter == null ? 0 : counter.get();
    }

    /**
     * Returns whether the retry-success path has handled the given order.
     */
    public boolean hasRetriedTask(String orderId) {
        return retriedTaskOrderIds.contains(orderId);
    }

    /**
     * Returns whether the dead-letter path has observed the given order.
     */
    public boolean hasDeadLetterTask(String orderId) {
        return deadLetterTaskOrderIds.contains(orderId);
    }

    /**
     * Returns the number of retry attempts observed for the given order.
     */
    public int retryAttempts(String orderId) {
        AtomicInteger counter = retryAttempts.get(orderId);
        return counter == null ? 0 : counter.get();
    }

    /**
     * Clears all recorded observation state.
     */
    public void reset() {
        handledTaskOrderIds.clear();
        retriedTaskOrderIds.clear();
        deadLetterTaskOrderIds.clear();
        domainEventCounts.clear();
        retryAttempts.clear();
    }
}
