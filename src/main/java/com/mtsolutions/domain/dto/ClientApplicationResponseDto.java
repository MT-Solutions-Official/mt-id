package com.mtsolutions.domain.dto;

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
                clientApplication.getCreatedAt(),
                clientApplication.getUpdatedAt(),
                clientApplication.getActive()
        );
    }
}
