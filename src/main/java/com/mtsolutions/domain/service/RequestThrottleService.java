package com.mtsolutions.domain.service;

import com.mtsolutions.application.utils.DateUtils;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class RequestThrottleService {

    private final Map<String, ThrottleState> states = new ConcurrentHashMap<>();
    private final DateUtils dateUtils;

    public RequestThrottleService(DateUtils dateUtils) {
        this.dateUtils = dateUtils;
    }

    public boolean shouldThrottle(String action, String identifier, int maxAttempts, Duration window, Duration minInterval) {
        String key = action + ":" + normalizeIdentifier(identifier);
        ThrottleState state = this.states.computeIfAbsent(key, ignored -> new ThrottleState());

        synchronized (state) {
            LocalDateTime now = this.dateUtils.now();

            if (state.lastAttemptAt != null && minInterval != null && !minInterval.isZero()
                    && now.isBefore(state.lastAttemptAt.plus(minInterval))) {
                return true;
            }

            if (window != null && !window.isZero()) {
                LocalDateTime threshold = now.minus(window);
                while (!state.attempts.isEmpty() && state.attempts.peekFirst().isBefore(threshold)) {
                    state.attempts.removeFirst();
                }
            }

            if (maxAttempts > 0 && state.attempts.size() >= maxAttempts) {
                return true;
            }

            state.attempts.addLast(now);
            state.lastAttemptAt = now;
            return false;
        }
    }

    private String normalizeIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return "unknown";
        }
        return identifier.trim().toLowerCase();
    }

    private static final class ThrottleState {
        private final Deque<LocalDateTime> attempts = new ArrayDeque<>();
        private LocalDateTime lastAttemptAt;
    }
}
