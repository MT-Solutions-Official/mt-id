package com.mtsolutions.domain.dto.request;

import com.mtsolutions.domain.constant.OwnerRole;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record AddOwnersToClientApplicationRequestDto(
        List<String> ownerIds,
        List<String> emails,
        OwnerRole role,
        @NotBlank(message = "Application ID cannot be blank")
        String appId
) {
}
