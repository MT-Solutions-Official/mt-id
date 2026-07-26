package com.mtsolutions.domain.dto.request;

import com.mtsolutions.domain.constant.UserRequiredField;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateClientApplicationRequestDto (
        @NotBlank(message = "Name is required")
        String name,
        @NotBlank(message = "Owner ID is required")
        String ownerId,
        String description,
        String logoUrl,
        EmailSettingsRequestDto emailSettings,
        @NotNull(message = "JWT expiration time is required")
        @Positive(message = "JWT expiration time must be a positive integer")
        Integer jwtExpirationInMinutes,
        @NotNull(message = "Refresh token expiration time is required")
        @Positive(message = "Refresh token expiration time must be a positive integer")
        Integer refreshTokenExpirationInDays,
        @NotEmpty(message = "Allowed origins list cannot be empty")
        List<String> allowedOrigins,
        List<UserRequiredField> requiredUserFields
) {
}
