package com.mtsolutions.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record UpdateClientApplicationSettingsRequestDto(
        @NotBlank(message = "App ID is required")
        String appId,
        List<String> allowedOrigins,
        @Positive(message = "JWT expiration time must be a positive integer")
        Integer jwtExpirationInMinutes,
        @Positive(message = "Refresh token expiration time must be a positive integer")
        Integer refreshTokenExpirationInDays,
        String googleAudience
) {
}
