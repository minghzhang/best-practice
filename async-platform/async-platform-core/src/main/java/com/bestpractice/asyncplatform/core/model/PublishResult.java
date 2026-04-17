package com.bestpractice.asyncplatform.core.model;

/**
 * Result returned after a publish attempt.
 *
 * @param success whether the publish operation succeeded
 * @param taskId the task or event identifier associated with the publish call
 * @param topic the target topic used for the publish attempt
 * @param message human-readable explanation for success or failure
 */
public record PublishResult(boolean success, String taskId, String topic, String message) {
    /**
     * Creates a successful publish result.
     */
    public static PublishResult success(String taskId, String topic) {
        return new PublishResult(true, taskId, topic, "published");
    }

    /**
     * Creates a failed publish result with an explanatory message.
     */
    public static PublishResult failure(String taskId, String topic, String message) {
        return new PublishResult(false, taskId, topic, message);
    }
}
