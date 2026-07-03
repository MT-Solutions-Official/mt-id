package com.mtsolutions.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRoleRequestDto(
        @NotBlank(message = "Application ID cannot be blank")
        String appId,
        @NotBlank(message = "Role name cannot be blank")
        String roleName
) {
}
