package com.mtsolutions.application.cache;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class AccountStatusCache {

    private record Entry(boolean disabled, Instant expiresAt) {}

    private final ConcurrentHashMap<String, Entry> entries = new ConcurrentHashMap<>();

    @ConfigProperty(name = "app.mt.id.account-status.cache.ttl.seconds", defaultValue = "30")
    Integer ttlSeconds;

    public Optional<Boolean> getUserDisabled(String userId) {
        return this.get("user:" + userId);
    }

    public Optional<Boolean> getOwnerDisabled(String ownerId) {
        return this.get("owner:" + ownerId);
    }

    public Optional<Boolean> getApplicationDisabled(String appId) {
        return this.get("app:" + appId);
    }

    public void putUserDisabled(String userId, boolean disabled) {
        this.put("user:" + userId, disabled);
    }

    public void putOwnerDisabled(String ownerId, boolean disabled) {
        this.put("owner:" + ownerId, disabled);
    }

    public void putApplicationDisabled(String appId, boolean disabled) {
        this.put("app:" + appId, disabled);
    }

    public void invalidateUser(String userId) {
        this.entries.remove("user:" + userId);
    }

    public void invalidateOwner(String ownerId) {
        this.entries.remove("owner:" + ownerId);
    }

    public void invalidateApplication(String appId) {
        this.entries.remove("app:" + appId);
    }

    private Optional<Boolean> get(String key) {
        Entry entry = this.entries.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (Instant.now().isAfter(entry.expiresAt())) {
            this.entries.remove(key, entry);
            return Optional.empty();
        }
        return Optional.of(entry.disabled());
    }

    private void put(String key, boolean disabled) {
        this.entries.put(key, new Entry(disabled, Instant.now().plus(this.ttl())));
    }

    private Duration ttl() {
        int seconds = this.ttlSeconds != null && this.ttlSeconds > 0 ? this.ttlSeconds : 30;
        return Duration.ofSeconds(seconds);
    }
}
