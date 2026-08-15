package com.mtsolutions.domain.dto.request;

import jakarta.validation.Valid;

public record UpdateOwnerRequestDto(
        String name,
        String phoneNumber,
        @Valid CreateDocumentRequestDto document
) {
}
