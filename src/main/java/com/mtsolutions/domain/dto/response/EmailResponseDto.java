package com.mtsolutions.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mtsolutions.domain.model.Email;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmailResponseDto(
        String email,
        Boolean primary,
        Boolean verified
) {

    public EmailResponseDto(Email email) {
        this(
                email.getEmail(),
                email.getPrimary(),
                email.getVerified()
        );
    }
}
