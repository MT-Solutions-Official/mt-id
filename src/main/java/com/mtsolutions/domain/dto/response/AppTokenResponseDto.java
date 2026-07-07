package com.mtsolutions.domain.dto.response;

public record AppTokenResponseDto(
        String accessToken,
        String tokenType,
        Long expiresIn
) {
}
