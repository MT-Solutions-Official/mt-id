package com.mtsolutions.domain.dto.request;

import com.mtsolutions.domain.constant.AddressResolutionMode;
import com.mtsolutions.domain.constant.Country;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAddressRequestDto(
        AddressResolutionMode mode,
        @NotNull(message = "Address country is required") Country country,
        @NotBlank(message = "Zip code is required") String zipCode,
        String street,
        @NotBlank(message = "Number is required") String number,
        String complement,
        String rt,
        String rw,
        String neighborhood,
        String kelurahan,
        String kecamatan,
        String city,
        String state
) {
}
