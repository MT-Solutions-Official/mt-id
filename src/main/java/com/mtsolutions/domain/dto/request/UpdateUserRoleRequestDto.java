package com.mtsolutions.domain.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRoleRequestDto(
        @NotBlank(message = "User role ID cannot be blank")
        String userRoleId,
        @NotBlank(message = "Role name cannot be blank")
        String roleName
) {
}
