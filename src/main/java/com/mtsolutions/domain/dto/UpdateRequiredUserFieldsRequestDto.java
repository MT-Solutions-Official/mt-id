package com.mtsolutions.domain.dto;

import com.mtsolutions.domain.constant.UserRequiredField;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateRequiredUserFieldsRequestDto(
        @NotBlank(message = "Application ID cannot be blank")
        String appId,
        @NotNull(message = "Required user fields list cannot be null")
        List<UserRequiredField> requiredUserFields
) {
}
