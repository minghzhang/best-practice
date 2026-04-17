package com.bestpractice.asyncplatform.core.model;

/**
 * Enumerates the runtime actions that can follow handler execution.
 */
public enum ConsumeDisposition {
    /**
     * The message was processed successfully and can be acknowledged.
     */
    SUCCESS,
    /**
     * The message should be re-routed to a retry path.
     */
    RETRY,
    /**
     * The message should be sent to the dead-letter path.
     */
    DLQ,
    /**
     * The message is considered malformed or unrecoverable and should be poison-routed.
     */
    POISON,
    /**
     * The message should be ignored without error.
     */
    SKIP
}
