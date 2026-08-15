package com.mtsolutions.domain.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record AddOwnersToClientApplicationRequestDto(
        List<String> ownerIds,
        List<String> emails,
        @NotBlank(message = "Application ID cannot be blank")
        String appId
) {
}
