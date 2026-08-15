package com.mtsolutions.domain.dto.request;

import com.mtsolutions.domain.constant.MaritalStatus;
import com.mtsolutions.domain.model.Phone;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@RegisterForReflection
public record GenerateUserGoogleTokenRequestDto(
        @NotBlank(message = "Google ID token is required")
        String idToken,
        @NotBlank(message = "App ID is required")
        String appId,
        String nonce,
        String name,
        String username,
        List<Phone> phones,
        CreateDocumentRequestDto document,
        MaritalStatus maritalStatus
) {
}
