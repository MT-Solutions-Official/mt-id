package com.mtsolutions.domain.dto.request;

import com.mtsolutions.domain.constant.UserRequiredField;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record UpdateClientApplicationSettingsRequestDto(
        @NotBlank(message = "App ID is required")
        String appId,
        String name,
        String description,
        String logoUrl,
        @Valid EmailSettingsRequestDto emailSettings,
        List<String> allowedOrigins,
        @Positive(message = "JWT expiration time must be a positive integer")
        Integer jwtExpirationInMinutes,
        @Positive(message = "Refresh token expiration time must be a positive integer")
        Integer refreshTokenExpirationInDays,
        String googleAudience,
        List<UserRequiredField> requiredUserFields
) {
}
