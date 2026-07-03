package com.mtsolutions.domain.dto.response;

import com.mtsolutions.domain.constant.UserRequiredField;
import com.mtsolutions.domain.entity.ClientApplication;

import java.time.LocalDateTime;
import java.util.List;

public record ClientApplicationResponseDto (
        String appId,
        String name,
        String description,
        String apiKey,
        String apiSecret,
        Integer jwtExpirationInMinutes,
        Integer refreshTokenExpirationInDays,
        List<String> allowedOrigins,
        List<UserRequiredField> requiredUserFields,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean active
) {

    public ClientApplicationResponseDto(ClientApplication clientApplication) {
        this(
                clientApplication.getAppId(),
                clientApplication.getName(),
                clientApplication.getDescription(),
                clientApplication.getApiKey(),
                clientApplication.getApiSecret(),
                clientApplication.getJwtExpirationInMinutes(),
                clientApplication.getRefreshTokenExpirationInDays(),
                clientApplication.getAllowedOrigins(),
                clientApplication.getRequiredUserFields(),
                clientApplication.getCreatedAt(),
                clientApplication.getUpdatedAt(),
                clientApplication.getActive()
        );
    }
}
