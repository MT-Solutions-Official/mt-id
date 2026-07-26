package com.mtsolutions.domain.dto.request;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record EmailSettingsRequestDto(
        String fromEmail,
        String fromName,
        String replyTo,
        String supportEmail,
        String supportUrl,
        String verificationRedirectUrl,
        String passwordResetRedirectUrl
) {
}

