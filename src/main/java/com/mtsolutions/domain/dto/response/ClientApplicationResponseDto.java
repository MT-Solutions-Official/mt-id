package com.mtsolutions.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mtsolutions.domain.constant.UserRequiredField;
import com.mtsolutions.domain.entity.ClientApplication;
import com.mtsolutions.domain.entity.Owner;
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
        List<OwnerResponseDto> owners,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean active
) {

    public ClientApplicationResponseDto(ClientApplication clientApplication) {
        this(clientApplication, null);
    }

    public ClientApplicationResponseDto(ClientApplication clientApplication, String apiSecret) {
        this(
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
                mapOwners(clientApplication.getOwners()),
                clientApplication.getCreatedAt(),
                clientApplication.getUpdatedAt(),
                clientApplication.getActive()
        );
    }

    private static List<OwnerResponseDto> mapOwners(List<Owner> owners) {
        if (owners == null || owners.isEmpty()) {
            return List.of();
        }
        return owners.stream().map(OwnerResponseDto::new).toList();
    }
}
