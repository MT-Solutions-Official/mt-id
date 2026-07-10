package com.mtsolutions.domain.dto.response;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record OwnerTokenResponseDto(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        Long refreshTokenExpiresIn
) {
}
