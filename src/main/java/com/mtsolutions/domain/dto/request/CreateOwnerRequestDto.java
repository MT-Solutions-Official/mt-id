package com.mtsolutions.domain.dto.request;

import com.mtsolutions.domain.constant.OwnerRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOwnerRequestDto (
        @NotBlank(message = "Name is required")
        String name,
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,
        @NotBlank(message = "Phone number is required")
        String phoneNumber,
        @NotNull(message = "Document is required")
        CreateDocumentRequestDto document,
        @NotBlank(message = "Password is required")
        String password,
        OwnerRole role) {
}
