package com.bestpractice.asyncplatform.core.model;

/**
 * Result returned by a handler after consuming one message.
 *
 * @param disposition the runtime action the platform should take next
 * @param reason human-readable explanation used for logs and debugging
 */
public record ConsumeResult(ConsumeDisposition disposition, String reason) {
    /**
     * Creates a successful consume result.
     */
    public static ConsumeResult success() {
        return new ConsumeResult(ConsumeDisposition.SUCCESS, "success");
    }

    /**
     * Creates a retry result with the supplied explanation.
     */
    public static ConsumeResult retry(String reason) {
        return new ConsumeResult(ConsumeDisposition.RETRY, reason);
    }

    /**
     * Creates a dead-letter result with the supplied explanation.
     */
    public static ConsumeResult dlq(String reason) {
        return new ConsumeResult(ConsumeDisposition.DLQ, reason);
    }

    /**
     * Creates a poison result with the supplied explanation.
     */
    public static ConsumeResult poison(String reason) {
        return new ConsumeResult(ConsumeDisposition.POISON, reason);
    }

    /**
     * Creates a skip result with the supplied explanation.
     */
    public static ConsumeResult skip(String reason) {
        return new ConsumeResult(ConsumeDisposition.SKIP, reason);
    }
}
