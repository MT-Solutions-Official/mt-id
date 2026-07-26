package com.mtsolutions.domain.dto.request;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;

@RegisterForReflection
public record ResetPasswordRequestDto(
        @NotBlank(message = "Token is required") String token,
        @NotBlank(message = "New password is required") String newPassword
) {
}

