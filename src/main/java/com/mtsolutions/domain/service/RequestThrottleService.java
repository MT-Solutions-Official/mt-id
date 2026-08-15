package com.mtsolutions.domain.service;

import com.mtsolutions.application.utils.DateUtils;
import com.mtsolutions.domain.entity.RequestThrottle;
import com.mtsolutions.domain.repository.RequestThrottleRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class RequestThrottleService {

    private final RequestThrottleRepository requestThrottleRepository;
    private final DateUtils dateUtils;

    public RequestThrottleService(RequestThrottleRepository requestThrottleRepository, DateUtils dateUtils) {
        this.requestThrottleRepository = requestThrottleRepository;
        this.dateUtils = dateUtils;
    }

    public boolean shouldThrottle(String action, String identifier, int maxAttempts, Duration window, Duration minInterval) {
        RequestThrottle throttle = this.load(action, identifier);
        LocalDateTime now = this.dateUtils.now();
        this.prune(throttle, now, window);

        if (this.isLimited(throttle, now, maxAttempts, minInterval)) {
            return true;
        }

        this.record(throttle, now);
        return false;
    }

    public boolean isThrottled(String action, String identifier, int maxAttempts, Duration window, Duration minInterval) {
        RequestThrottle throttle = this.load(action, identifier);
        LocalDateTime now = this.dateUtils.now();
        this.prune(throttle, now, window);
        this.requestThrottleRepository.persistOrUpdate(throttle);
        return this.isLimited(throttle, now, maxAttempts, minInterval);
    }

    public void recordAttempt(String action, String identifier, Duration window) {
        RequestThrottle throttle = this.load(action, identifier);
        LocalDateTime now = this.dateUtils.now();
        this.prune(throttle, now, window);
        this.record(throttle, now);
    }

    public void clear(String action, String identifier) {
        this.requestThrottleRepository.findByKey(this.key(action, identifier))
                .ifPresent(this.requestThrottleRepository::delete);
    }

    private boolean isLimited(RequestThrottle throttle, LocalDateTime now, int maxAttempts, Duration minInterval) {
        if (throttle.getLastAttemptAt() != null && minInterval != null && !minInterval.isZero()
                && now.isBefore(throttle.getLastAttemptAt().plus(minInterval))) {
            return true;
        }
        return maxAttempts > 0 && throttle.getAttempts().size() >= maxAttempts;
    }

    private void record(RequestThrottle throttle, LocalDateTime now) {
        throttle.getAttempts().add(now);
        throttle.setLastAttemptAt(now);
        throttle.setUpdatedAt(now);
        this.requestThrottleRepository.persistOrUpdate(throttle);
    }

    private void prune(RequestThrottle throttle, LocalDateTime now, Duration window) {
        if (throttle.getAttempts() == null) {
            throttle.setAttempts(new ArrayList<>());
        }
        if (window == null || window.isZero()) {
            return;
        }
        LocalDateTime threshold = now.minus(window);
        throttle.getAttempts().removeIf(attempt -> attempt == null || attempt.isBefore(threshold));
    }

    private RequestThrottle load(String action, String identifier) {
        String key = this.key(action, identifier);
        return this.requestThrottleRepository.findByKey(key).orElseGet(() -> RequestThrottle.builder()
                .key(key)
                .attempts(new ArrayList<>())
                .build());
    }

    private String key(String action, String identifier) {
        return action + ":" + normalizeIdentifier(identifier);
    }

    private String normalizeIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return "unknown";
        }
        return identifier.trim().toLowerCase();
    }
}
