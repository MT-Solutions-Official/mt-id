package com.mtsolutions.domain.dto.request;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record GenerateUserTokenRequestDto(String email, String password) {
}
