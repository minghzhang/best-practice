package com.bestpractice.asyncplatform.core.model;

/**
 * Delivery-related metadata attached to an async message.
 *
 * <p>This metadata is used by the runtime for retry budgeting, dedupe, delay and
 * message lifetime management.</p>
 */
public class DeliveryMetadata {
    /**
     * Current delivery attempt count.
     */
    private int attempt = 0;
    /**
     * Maximum number of attempts allowed before the message stops retrying.
     */
    private int maxAttempts = 3;
    /**
     * Epoch milliseconds before which the message should not be processed.
     */
    private Long notBeforeEpochMillis;
    /**
     * Business-level dedupe key reserved for idempotency handling.
     */
    private String dedupeKey;
    /**
     * Optional time-to-live for the message.
     */
    private Long ttlMillis;
    /**
     * Original topic before the message was moved to retry or DLQ.
     */
    private String originTopic;

    /**
     * Returns the current delivery attempt count.
     */
    public int getAttempt() { return attempt; }
    /**
     * Updates the current delivery attempt count.
     */
    public void setAttempt(int attempt) { this.attempt = attempt; }
    /**
     * Returns the maximum number of retry attempts allowed.
     */
    public int getMaxAttempts() { return maxAttempts; }
    /**
     * Updates the maximum number of retry attempts allowed.
     */
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
    /**
     * Returns the earliest allowed processing time in epoch milliseconds.
     */
    public Long getNotBeforeEpochMillis() { return notBeforeEpochMillis; }
    /**
     * Updates the earliest allowed processing time in epoch milliseconds.
     */
    public void setNotBeforeEpochMillis(Long notBeforeEpochMillis) { this.notBeforeEpochMillis = notBeforeEpochMillis; }
    /**
     * Returns the dedupe key reserved for idempotency logic.
     */
    public String getDedupeKey() { return dedupeKey; }
    /**
     * Updates the dedupe key reserved for idempotency logic.
     */
    public void setDedupeKey(String dedupeKey) { this.dedupeKey = dedupeKey; }
    /**
     * Returns the configured time-to-live in milliseconds.
     */
    public Long getTtlMillis() { return ttlMillis; }
    /**
     * Updates the configured time-to-live in milliseconds.
     */
    public void setTtlMillis(Long ttlMillis) { this.ttlMillis = ttlMillis; }
    /**
     * Returns the original topic before rerouting.
     */
    public String getOriginTopic() { return originTopic; }
    /**
     * Updates the original topic before rerouting.
     */
    public void setOriginTopic(String originTopic) { this.originTopic = originTopic; }

    /**
     * Copies delivery metadata so the runtime can mutate retry bookkeeping safely.
     */
    public DeliveryMetadata copy() {
        DeliveryMetadata copy = new DeliveryMetadata();
        copy.attempt = attempt;
        copy.maxAttempts = maxAttempts;
        copy.notBeforeEpochMillis = notBeforeEpochMillis;
        copy.dedupeKey = dedupeKey;
        copy.ttlMillis = ttlMillis;
        copy.originTopic = originTopic;
        return copy;
    }
}
