package com.mtsolutions.domain.dto.request;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Email;

@RegisterForReflection
public record UpdateUserRequestDto(
        String name,
        @Email(message = "Email should be valid")
        String email,
        String currentPassword,
        String newPassword
) {
}
