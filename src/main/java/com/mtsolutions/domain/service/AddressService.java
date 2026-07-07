package com.mtsolutions.domain.service;

import com.mtsolutions.application.client.kodepos.KodePosAddressDto;
import com.mtsolutions.application.client.kodepos.KodePosClient;
import com.mtsolutions.application.client.kodepos.KodePosResponseDto;
import com.mtsolutions.application.client.viacep.ViaCepClient;
import com.mtsolutions.application.client.viacep.ViaCepResponseDto;
import com.mtsolutions.application.client.zippopotam.ZippopotamClient;
import com.mtsolutions.application.client.zippopotam.ZippopotamPlaceDto;
import com.mtsolutions.application.client.zippopotam.ZippopotamResponseDto;
import com.mtsolutions.application.exception.*;
import com.mtsolutions.domain.constant.AddressResolutionMode;
import com.mtsolutions.domain.constant.Country;
import com.mtsolutions.domain.dto.request.CreateAddressRequestDto;
import com.mtsolutions.domain.model.Address;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
@Slf4j
public class AddressService {

    private final ViaCepClient viaCepClient;
    private final KodePosClient kodePosClient;
    private final ZippopotamClient zippopotamClient;

    public AddressService(@RestClient ViaCepClient viaCepClient,
                          @RestClient KodePosClient kodePosClient,
                          @RestClient ZippopotamClient zippopotamClient) {
        this.viaCepClient = viaCepClient;
        this.kodePosClient = kodePosClient;
        this.zippopotamClient = zippopotamClient;
    }

    // BRAZILIAN ADDRESS
    public Address getBrazilianAddressFromZipcode(String zipCode, String number, String complement) {
        log.info("Fetching address from ViaCEP for CEP: {}", zipCode);
        try {
            ViaCepResponseDto response = this.viaCepClient.getAddress(zipCode);
            if (!response.isValid()) throw new ViaCepCepNotFoundException();
            return Address.builder()
                    .zipCode(response.getZipCode())
                    .street(response.getStreet())
                    .neighborhood(response.getNeighborhood())
                    .city(response.getCity())
                    .state(response.getState())
                    .number(number)
                    .complement(complement)
                    .build();
        } catch (ViaCepCepNotFoundException e) {
            throw e;
        } catch (WebApplicationException e) {
            String externalResponse = e.getResponse().readEntity(String.class);
            if (e.getResponse().getStatus() == 400) throw new ViaCepInvalidCepException(externalResponse);
            log.error("ViaCEP API error for CEP {}: {}", zipCode, e.getMessage());
            throw new ViaCepApiException(externalResponse);
        } catch (Exception e) {
            log.error("ViaCEP unexpected error for CEP {}: {}", zipCode, e.getMessage());
            throw new ViaCepApiException(e.getMessage());
        }
    }

    // INDONESIAN ADDRESS
    public Address getIndonesianAddressFromZipcode(String zipCode, String street, String number,
                                                   String rt, String rw, String complement) {
        log.info("Fetching address from KodePos for ZIP: {}", zipCode);
        try {
            KodePosResponseDto response = this.kodePosClient.getAddress(zipCode);
            if (!response.isValid()) throw new KodePosZipNotFoundException();
            KodePosAddressDto place = response.getData().getFirst();
            return Address.builder()
                    .zipCode(String.valueOf(place.getZipCode()))
                    .street(street)
                    .number(number)
                    .rt(rt)
                    .rw(rw)
                    .kelurahan(place.getKelurahan())
                    .kecamatan(place.getKecamatan())
                    .city(place.getCity())
                    .state(place.getProvince())
                    .complement(complement)
                    .build();
        } catch (KodePosZipNotFoundException e) {
            throw e;
        } catch (WebApplicationException e) {
            String externalResponse = e.getResponse().readEntity(String.class);
            log.error("KodePos API error for ZIP {}: {}", zipCode, e.getMessage());
            throw new KodePosApiException(externalResponse);
        } catch (Exception e) {
            log.error("KodePos API error for ZIP {}: {}", zipCode, e.getMessage());
            throw new KodePosApiException(e.getMessage());
        }
    }

    // AMERICAN ADDRESS
    public Address getAmericanAddressFromZipcode(String zipCode, String street, String number, String complement) {
        log.info("Fetching address from Zippopotam for US ZIP: {}", zipCode);
        return getZippopotamAddress("us", zipCode, street, number, complement);
    }

    // PORTUGUESE ADDRESS
    public Address getPortugueseAddressFromZipcode(String zipCode, String street, String number, String complement) {
        log.info("Fetching address from Zippopotam for PT ZIP: {}", zipCode);
        return getZippopotamAddress("pt", zipCode, street, number, complement);
    }

    protected Address getZippopotamAddress(String country, String zipCode, String street, String number, String complement) {
        try {
            ZippopotamResponseDto response = this.zippopotamClient.getAddress(country, zipCode);
            if (!response.isValid()) throw new ZippopotamZipNotFoundException();
            ZippopotamPlaceDto place = response.getPlaces().getFirst();
            return Address.builder()
                    .zipCode(response.getPostCode())
                    .street(street)
                    .number(number)
                    .city(place.getCity())
                    .state(place.getState())
                    .complement(complement)
                    .build();
        } catch (ZippopotamZipNotFoundException e) {
            throw e;
        } catch (WebApplicationException e) {
            String externalResponse = e.getResponse().readEntity(String.class);
            if (e.getResponse().getStatus() == 404) throw new ZippopotamZipNotFoundException(externalResponse);
            log.error("Zippopotam API error for {} ZIP {}: {}", country.toUpperCase(), zipCode, e.getMessage());
            throw new ZippopotamApiException(externalResponse);
        } catch (Exception e) {
            log.error("Zippopotam unexpected error for {} ZIP {}: {}", country.toUpperCase(), zipCode, e.getMessage());
            throw new ZippopotamApiException(e.getMessage());
        }
    }

    public Address resolveAddress(CreateAddressRequestDto addressRequest) {
        AddressResolutionMode resolutionMode = addressRequest.mode() != null
                ? addressRequest.mode()
                : AddressResolutionMode.AUTO;

        return switch (resolutionMode) {
            case AUTO -> this.resolveAddressAutomatically(addressRequest);
            case MANUAL -> this.resolveAddressManually(addressRequest);
        };
    }

    private Address resolveAddressAutomatically(CreateAddressRequestDto addressRequest) {
        Country addressCountry = addressRequest.country();
        String zipCode = normalizeZipCode(addressCountry, addressRequest.zipCode());
        String number = requireField("number", addressRequest.number());
        String complement = trimToNull(addressRequest.complement());

        Address resolvedAddress = switch (addressCountry) {
            case BR -> this.getBrazilianAddressFromZipcode(zipCode, number, complement);
            case US -> this.getAmericanAddressFromZipcode(
                    zipCode,
                    requireField("street", addressRequest.street()),
                    number,
                    complement
            );
            case PT -> this.getPortugueseAddressFromZipcode(
                    zipCode,
                    requireField("street", addressRequest.street()),
                    number,
                    complement
            );
            case ID -> this.getIndonesianAddressFromZipcode(
                    zipCode,
                    requireField("street", addressRequest.street()),
                    number,
                    requireField("rt", addressRequest.rt()),
                    requireField("rw", addressRequest.rw()),
                    complement
            );
            default -> throw new BadRequestException(
                    "Address country '" + addressCountry + "' is not supported for user address creation. Supported countries: BR, US, PT, ID."
            );
        };

        resolvedAddress.setCountry(addressCountry);
        return resolvedAddress;
    }

    private Address resolveAddressManually(CreateAddressRequestDto addressRequest) {
        Country addressCountry = addressRequest.country();
        String zipCode = normalizeZipCode(addressCountry, addressRequest.zipCode());
        String street = requireField("street", addressRequest.street());
        String number = requireField("number", addressRequest.number());
        String city = requireField("city", addressRequest.city());
        String state = requireField("state", addressRequest.state());
        String complement = trimToNull(addressRequest.complement());

        Address.AddressBuilder addressBuilder = Address.builder()
                .country(addressCountry)
                .zipCode(zipCode)
                .street(street)
                .number(number)
                .complement(complement)
                .city(city)
                .state(state);

        switch (addressCountry) {
            case BR -> addressBuilder.neighborhood(requireField("neighborhood", addressRequest.neighborhood()));
            case ID -> {
                addressBuilder.rt(requireField("rt", addressRequest.rt()));
                addressBuilder.rw(requireField("rw", addressRequest.rw()));
                addressBuilder.kelurahan(requireField("kelurahan", addressRequest.kelurahan()));
                addressBuilder.kecamatan(requireField("kecamatan", addressRequest.kecamatan()));
            }
            case US, PT -> {
            }
            default -> throw new BadRequestException(
                    "Address country '" + addressCountry + "' is not supported for user address creation. Supported countries: BR, US, PT, ID."
            );
        }

        return addressBuilder.build();
    }

    private String requireField(String fieldName, String value) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new BadRequestException("Address field '" + fieldName + "' is required.");
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeZipCode(Country country, String zipCode) {
        String normalized = requireField("zipCode", zipCode);

        return switch (country) {
            case BR, US, ID -> normalized.replaceAll("\\D", "");
            case PT -> normalized.toUpperCase();
            default -> normalized;
        };
    }
}