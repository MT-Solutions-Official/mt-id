package com.mtsolutions.application.cache;

import com.mtsolutions.domain.entity.ClientApplication;
import com.mtsolutions.domain.repository.ClientApplicationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class AllowedOriginCache {

    private record CachedApp(boolean active, Set<String> origins, Instant expiresAt) {}

    private final ConcurrentHashMap<String, CachedApp> apps = new ConcurrentHashMap<>();
    private final ClientApplicationRepository clientApplicationRepository;

    @ConfigProperty(name = "app.mt.id.cors.cache.ttl.seconds", defaultValue = "60")
    Integer ttlSeconds;

    public AllowedOriginCache(ClientApplicationRepository clientApplicationRepository) {
        this.clientApplicationRepository = clientApplicationRepository;
    }

    public boolean isOriginAllowed(String appId, String origin) {
        String normalizedAppId = appId != null ? appId.trim() : null;
        String normalizedOrigin = normalizeOrigin(origin);
        if (normalizedAppId == null || normalizedAppId.isBlank() || normalizedOrigin == null) {
            return false;
        }

        CachedApp cached = this.load(normalizedAppId);
        return cached.active() && cached.origins().contains(normalizedOrigin);
    }

    public void invalidate(String appId) {
        if (appId != null) {
            this.apps.remove(appId.trim());
        }
    }

    public static String normalizeOrigin(String origin) {
        if (origin == null || origin.isBlank()) {
            return null;
        }
        String trimmed = origin.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    private CachedApp load(String appId) {
        CachedApp cached = this.apps.get(appId);
        if (cached != null && Instant.now().isBefore(cached.expiresAt())) {
            return cached;
        }

        ClientApplication application;
        try {
            application = this.clientApplicationRepository.findClientApplicationById(appId);
        } catch (RuntimeException e) {
            CachedApp missing = new CachedApp(false, Set.of(), Instant.now().plus(this.ttl()));
            this.apps.put(appId, missing);
            return missing;
        }

        Set<String> origins = new HashSet<>();
        if (application.getAllowedOrigins() != null) {
            for (String allowed : application.getAllowedOrigins()) {
                String normalized = normalizeOrigin(allowed);
                if (normalized != null) {
                    origins.add(normalized);
                }
            }
        }
        CachedApp fresh = new CachedApp(
                !Boolean.FALSE.equals(application.getActive()),
                Set.copyOf(origins),
                Instant.now().plus(this.ttl())
        );
        this.apps.put(appId, fresh);
        return fresh;
    }

    private Duration ttl() {
        int seconds = this.ttlSeconds != null && this.ttlSeconds > 0 ? this.ttlSeconds : 60;
        return Duration.ofSeconds(seconds);
    }
}
