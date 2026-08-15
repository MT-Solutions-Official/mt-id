package com.mtsolutions.domain.dto.request;

import com.mtsolutions.domain.constant.OwnerRole;
import jakarta.validation.constraints.NotNull;

public record UpdateAppOwnerRoleRequestDto(
        @NotNull(message = "Role is required") OwnerRole role
) {
}
