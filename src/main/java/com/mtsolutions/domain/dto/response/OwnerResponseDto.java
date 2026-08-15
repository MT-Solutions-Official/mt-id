package com.mtsolutions.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mtsolutions.domain.constant.OwnerRole;
import com.mtsolutions.domain.entity.Owner;
import com.mtsolutions.domain.model.Document;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OwnerResponseDto(
        String ownerId,
        String name,
        EmailResponseDto email,
        PhoneResponseDto phone,
        Document document,
        OwnerRole role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime disabledAt,
        Boolean active
) {

    public OwnerResponseDto(Owner owner) {
        this(
                owner.getOwnerId(),
                owner.getName(),
                owner.getEmail() != null ? new EmailResponseDto(owner.getEmail()) : null,
                owner.getPhone() != null ? new PhoneResponseDto(owner.getPhone()) : null,
                owner.getDocument(),
                owner.getRole(),
                owner.getCreatedAt(),
                owner.getUpdatedAt(),
                owner.getDisabledAt(),
                owner.getActive()
        );
    }
}
