package com.mtsolutions.application.utils;

public final class NormalizeUtils {

    private NormalizeUtils() {
    }

    public static String normalizeEmail(String email) {
        String trimmed = trimToNull(email);
        return trimmed != null ? trimmed.toLowerCase() : null;
    }

    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
