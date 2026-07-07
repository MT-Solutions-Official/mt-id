package com.mtsolutions.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mtsolutions.domain.constant.Country;
import com.mtsolutions.domain.model.Address;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AddressResponseDto(

        Country country,
        String zipCode,
        String street,
        String number,
        String complement,
        String rt,
        String rw,
        String neighborhood,
        String kelurahan,
        String kecamatan,
        String city,
        String state
) {

    public AddressResponseDto(Address address) {
        this(
                address.getCountry(),
                address.getZipCode(),
                address.getStreet(),
                address.getNumber(),
                address.getComplement(),
                address.getRt(),
                address.getRw(),
                address.getNeighborhood(),
                address.getKelurahan(),
                address.getKecamatan(),
                address.getCity(),
                address.getState()
        );
    }
}
