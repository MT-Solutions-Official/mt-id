package com.mtsolutions.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mtsolutions.domain.constant.OwnerRole;
import com.mtsolutions.domain.entity.Owner;
import com.mtsolutions.domain.model.UserImage;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AppOwnerResponseDto(
        String ownerId,
        String name,
        EmailResponseDto email,
        PhoneResponseDto phone,
        List<UserImage> images,
        OwnerRole role,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public AppOwnerResponseDto(Owner owner, OwnerRole role) {
        this(
                owner.getOwnerId(),
                owner.getName(),
                owner.getEmail() != null ? new EmailResponseDto(owner.getEmail()) : null,
                owner.getPhone() != null ? new PhoneResponseDto(owner.getPhone()) : null,
                owner.getImages(),
                role != null ? role : OwnerRole.OWNER_VIEWER,
                owner.getActive(),
                owner.getCreatedAt(),
                owner.getUpdatedAt()
        );
    }
}
