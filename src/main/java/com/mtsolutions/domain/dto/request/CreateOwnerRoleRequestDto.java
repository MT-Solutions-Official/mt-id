package com.mtsolutions.domain.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateOwnerRoleRequestDto(
        @NotBlank(message = "Role name cannot be blank")
        String roleName
) {
}
