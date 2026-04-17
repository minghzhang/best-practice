package com.bestpractice.asyncplatform.core.model;

/**
 * Routing metadata used by the platform runtime.
 *
 * <p>These fields are intentionally transport-neutral and describe the business
 * dimensions the runtime may later use for partitioning, sharding or cluster-aware routing.</p>
 */
public class RoutingMetadata {
    /**
     * Account identifier associated with the message.
     */
    private String accountId;
    /**
     * Region associated with the message.
     */
    private String region;
    /**
     * Cluster associated with the message.
     */
    private String cluster;
    /**
     * Tenant shard or logical partition hint.
     */
    private String tenantShard;
    /**
     * Key used when ordered consumption must be preserved.
     */
    private String sequentialKey;
    /**
     * Broadcast scope hint reserved for wider fan-out scenarios.
     */
    private String broadcastScope;

    /**
     * Returns the account identifier associated with the message.
     */
    public String getAccountId() { return accountId; }
    /**
     * Updates the account identifier associated with the message.
     */
    public void setAccountId(String accountId) { this.accountId = accountId; }
    /**
     * Returns the region associated with the message.
     */
    public String getRegion() { return region; }
    /**
     * Updates the region associated with the message.
     */
    public void setRegion(String region) { this.region = region; }
    /**
     * Returns the cluster associated with the message.
     */
    public String getCluster() { return cluster; }
    /**
     * Updates the cluster associated with the message.
     */
    public void setCluster(String cluster) { this.cluster = cluster; }
    /**
     * Returns the tenant shard hint.
     */
    public String getTenantShard() { return tenantShard; }
    /**
     * Updates the tenant shard hint.
     */
    public void setTenantShard(String tenantShard) { this.tenantShard = tenantShard; }
    /**
     * Returns the sequential ordering key.
     */
    public String getSequentialKey() { return sequentialKey; }
    /**
     * Updates the sequential ordering key.
     */
    public void setSequentialKey(String sequentialKey) { this.sequentialKey = sequentialKey; }
    /**
     * Returns the broadcast scope hint.
     */
    public String getBroadcastScope() { return broadcastScope; }
    /**
     * Updates the broadcast scope hint.
     */
    public void setBroadcastScope(String broadcastScope) { this.broadcastScope = broadcastScope; }
}
