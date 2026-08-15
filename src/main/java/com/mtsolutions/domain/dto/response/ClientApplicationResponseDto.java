package com.mtsolutions.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mtsolutions.domain.constant.UserRequiredField;
import com.mtsolutions.domain.entity.ClientApplication;
import com.mtsolutions.domain.model.EmailSettings;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ClientApplicationResponseDto(
        String appId,
        String name,
        String description,
        String logoUrl,
        EmailSettings emailSettings,
        String apiKey,
        String apiSecret,
        Integer jwtExpirationInMinutes,
        Integer refreshTokenExpirationInDays,
        List<String> allowedOrigins,
        String googleAudience,
        List<UserRequiredField> requiredUserFields,
        List<AppOwnerResponseDto> owners,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean active
) {

    public static ClientApplicationResponseDto from(ClientApplication clientApplication,
                                                    List<AppOwnerResponseDto> owners,
                                                    String apiSecret) {
        return new ClientApplicationResponseDto(
                clientApplication.getAppId(),
                clientApplication.getName(),
                clientApplication.getDescription(),
                clientApplication.getLogoUrl(),
                clientApplication.getEmailSettings(),
                clientApplication.getApiKey(),
                apiSecret,
                clientApplication.getJwtExpirationInMinutes(),
                clientApplication.getRefreshTokenExpirationInDays(),
                clientApplication.getAllowedOrigins(),
                clientApplication.getGoogleAudience(),
                clientApplication.getRequiredUserFields(),
                owners != null ? owners : List.of(),
                clientApplication.getCreatedAt(),
                clientApplication.getUpdatedAt(),
                clientApplication.getActive()
        );
    }
}
