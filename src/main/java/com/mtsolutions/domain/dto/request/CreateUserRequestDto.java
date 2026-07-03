package com.mtsolutions.domain.dto.request;

import com.mtsolutions.domain.constant.MaritalStatus;
import com.mtsolutions.domain.model.Phone;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateUserRequestDto (
        @NotBlank(message = "App ID is required")
        String appId,
        String name,
        String username,
        List<String> email,
        String password,
        List<Phone> phones,
        CreateDocumentRequestDto document,
        MaritalStatus maritalStatus,
        List<String> roles

) {
}
