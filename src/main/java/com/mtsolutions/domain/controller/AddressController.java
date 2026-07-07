package com.mtsolutions.domain.controller;

import com.mtsolutions.domain.model.Address;
import com.mtsolutions.domain.service.AddressService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    public Address getBrazilianAddressByZipCode(String zipCode, String number, String complement) {
        return this.addressService.getBrazilianAddressFromZipcode(zipCode, number, complement);
    }

    public Address getIndonesianAddressByZipCode(String zipCode, String street, String number,
                                                 String rt, String rw, String complement) {
        return this.addressService.getIndonesianAddressFromZipcode(zipCode, street, number, rt, rw, complement);
    }

    public Address getAmericanAddressByZipCode(String zipCode, String street, String number, String complement) {
        return this.addressService.getAmericanAddressFromZipcode(zipCode, street, number, complement);
    }

    public Address getPortugueseAddressByZipCode(String zipCode, String street, String number, String complement) {
        return this.addressService.getPortugueseAddressFromZipcode(zipCode, street, number, complement);
    }
}
