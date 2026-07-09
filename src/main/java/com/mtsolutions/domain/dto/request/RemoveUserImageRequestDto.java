package com.mtsolutions.domain.dto.request;

import com.mtsolutions.domain.constant.ImageType;

public record RemoveUserImageRequestDto(
        String userId,
        ImageType imageType
) {
}
