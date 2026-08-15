package com.mtsolutions.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mtsolutions.domain.model.Phone;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PhoneResponseDto(
        String phoneNumber,
        Boolean verified
) {

    public PhoneResponseDto(Phone phone) {
        this(
                phone.getPhoneNumber(),
                phone.getVerified()
        );
    }
}
