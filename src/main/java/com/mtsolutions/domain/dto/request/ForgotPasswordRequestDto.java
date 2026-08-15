package com.mtsolutions.domain.dto.request;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;

@RegisterForReflection
public record ForgotPasswordRequestDto(
        @NotBlank(message = "Email is required") String email,
        @NotBlank(message = "App ID is required") String appId
) {
}
