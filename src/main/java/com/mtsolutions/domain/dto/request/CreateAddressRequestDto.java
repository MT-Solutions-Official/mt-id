package com.mtsolutions.domain.dto.request;

import com.mtsolutions.domain.constant.Country;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAddressRequestDto(
        @NotNull(message = "Address country is required") Country country,
        @NotBlank(message = "Zip code is required") String zipCode,
        @NotBlank(message = "Street is required") String street,
        @NotBlank(message = "Number is required") String number,
        String complement,
        String rt,
        String rw,
        String neighborhood,
        String kelurahan,
        String kecamatan,
        @NotBlank(message = "City is required") String city,
        @NotBlank(message = "State is required") String state
) {
}
