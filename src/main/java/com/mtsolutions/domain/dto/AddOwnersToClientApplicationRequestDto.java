package com.mtsolutions.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AddOwnersToClientApplicationRequestDto (
        @NotEmpty(message = "Owner IDs cannot be empty")
        List<String> ownerIds,

        @NotBlank(message = "Application ID cannot be blank")
        String appId
) {
}
