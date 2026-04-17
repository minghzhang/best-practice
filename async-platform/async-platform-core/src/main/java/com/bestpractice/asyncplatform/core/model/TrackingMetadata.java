package com.bestpractice.asyncplatform.core.model;

/**
 * Cross-cutting tracking metadata attached to each async message.
 *
 * <p>These fields help preserve request context, tenant identity and source service
 * information across asynchronous boundaries.</p>
 */
public class TrackingMetadata {
    /**
     * Request identifier from the originating request flow.
     */
    private String requestId;
    /**
     * Tenant identifier associated with the message.
     */
    private String tenantId;
    /**
     * User identifier associated with the message.
     */
    private String userId;
    /**
     * Originating service name.
     */
    private String sourceService;
    /**
     * Domain or business context associated with the message.
     */
    private String domain;
    /**
     * Locale associated with the originating request.
     */
    private String locale;

    /**
     * Returns the originating request identifier.
     */
    public String getRequestId() { return requestId; }
    /**
     * Updates the originating request identifier.
     */
    public void setRequestId(String requestId) { this.requestId = requestId; }
    /**
     * Returns the tenant identifier associated with the message.
     */
    public String getTenantId() { return tenantId; }
    /**
     * Updates the tenant identifier associated with the message.
     */
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    /**
     * Returns the user identifier associated with the message.
     */
    public String getUserId() { return userId; }
    /**
     * Updates the user identifier associated with the message.
     */
    public void setUserId(String userId) { this.userId = userId; }
    /**
     * Returns the originating service name.
     */
    public String getSourceService() { return sourceService; }
    /**
     * Updates the originating service name.
     */
    public void setSourceService(String sourceService) { this.sourceService = sourceService; }
    /**
     * Returns the domain or business context.
     */
    public String getDomain() { return domain; }
    /**
     * Updates the domain or business context.
     */
    public void setDomain(String domain) { this.domain = domain; }
    /**
     * Returns the locale associated with the message.
     */
    public String getLocale() { return locale; }
    /**
     * Updates the locale associated with the message.
     */
    public void setLocale(String locale) { this.locale = locale; }
}
