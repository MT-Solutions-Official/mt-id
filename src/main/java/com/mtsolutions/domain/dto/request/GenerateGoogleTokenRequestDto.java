package com.mtsolutions.domain.dto.request;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;

@RegisterForReflection
public record GenerateGoogleTokenRequestDto(
        @NotBlank(message = "Google ID token is required")
        String idToken
) {
}
