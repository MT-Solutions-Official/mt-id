package com.mtsolutions.domain.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateOwnerRoleRequestDto(
        @NotBlank(message = "Owner role ID cannot be blank")
        String ownerRoleId,
        @NotBlank(message = "Role name cannot be blank")
        String roleName
) {
}
