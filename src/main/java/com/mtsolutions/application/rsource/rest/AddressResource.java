package com.mtsolutions.application.rsource.rest;

import com.mtsolutions.application.common.RequestParams;
import com.mtsolutions.application.rsource.rest.examples.AddressExamples;
import com.mtsolutions.domain.controller.AddressController;
import com.mtsolutions.domain.dto.response.AddressResponseDto;
import com.mtsolutions.domain.model.Address;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@RequestScoped
@Path("/addresses")
@Tag(name = "Addresses", description = "Lookup addresses by ZIP code")
public class AddressResource {

    private final AddressController addressController;

    public AddressResource(AddressController addressController) {
        this.addressController = addressController;
    }

    @GET
    @Path("/br/{zipCode}")
    @PermitAll
    @Operation(
            summary     = "Lookup Brazilian address by ZIP code",
            description = "Fetches address from ViaCEP API. Provide number and complement manually."
    )
    @APIResponse(
            responseCode = "200",
            description  = "Address found",
            content      = @Content(
                    mediaType = "application/json",
                    examples  = @ExampleObject(name = "Success", value = AddressExamples.BR_RESPONSE)
            )
    )
    @APIResponse(responseCode = "400", description = "Invalid ZIP code")
    @APIResponse(responseCode = "404", description = "ZIP code not found")
    @APIResponse(responseCode = "503", description = "ViaCEP unavailable")
    public Response getBrazilianAddressByZipCode(
            @PathParam(RequestParams.ZIP_CODE)    String zipCode,
            @QueryParam(RequestParams.NUMBER)     String number,
            @QueryParam(RequestParams.COMPLEMENT) String complement) {

        Address address = this.addressController.getBrazilianAddressByZipCode(zipCode, number, complement);

        return Response.status(Response.Status.OK)
                .entity(new AddressResponseDto(address))
                .build();
    }

    @GET
    @Path("/id/{zipCode}")
    @PermitAll
    @Operation(
            summary     = "Lookup Indonesian address by ZIP code",
            description = "Fetches kelurahan, kecamatan, city and province from KodePos API. Street, number, RT and RW must be provided manually."
    )
    @APIResponse(
            responseCode = "200",
            description  = "Address found",
            content      = @Content(
                    mediaType = "application/json",
                    examples  = @ExampleObject(name = "Success", value = AddressExamples.ID_RESPONSE)
            )
    )
    @APIResponse(responseCode = "400", description = "Missing required parameters or invalid ZIP code")
    @APIResponse(responseCode = "404", description = "ZIP code not found")
    @APIResponse(responseCode = "503", description = "KodePos unavailable")
    public Response getIndonesianAddressByZipCode(
            @PathParam(RequestParams.ZIP_CODE)    String zipCode,
            @NotBlank(message = "Street is required") @QueryParam(RequestParams.STREET) String street,
            @NotBlank(message = "Number is required") @QueryParam(RequestParams.NUMBER) String number,
            @NotBlank(message = "RT is required")     @QueryParam(RequestParams.RT)     String rt,
            @NotBlank(message = "RW is required")     @QueryParam(RequestParams.RW)     String rw,
            @QueryParam(RequestParams.COMPLEMENT) String complement) {

        Address address = this.addressController.getIndonesianAddressByZipCode(zipCode, street, number, rt, rw, complement);

        return Response.status(Response.Status.OK)
                .entity(new AddressResponseDto(address))
                .build();
    }

    @GET
    @Path("/us/{zipCode}")
    @PermitAll
    @Operation(
            summary     = "Lookup American address by ZIP code",
            description = "Fetches city and state from Zippopotam API. Street and number must be provided manually."
    )
    @APIResponse(
            responseCode = "200",
            description  = "Address found",
            content      = @Content(
                    mediaType = "application/json",
                    examples  = @ExampleObject(name = "Success", value = AddressExamples.US_RESPONSE)
            )
    )
    @APIResponse(responseCode = "400", description = "Missing required parameters or invalid ZIP code")
    @APIResponse(responseCode = "404", description = "ZIP code not found")
    @APIResponse(responseCode = "503", description = "Zippopotam unavailable")
    public Response getAmericanAddressByZipCode(
            @PathParam(RequestParams.ZIP_CODE)    String zipCode,
            @NotBlank(message = "Street is required") @QueryParam(RequestParams.STREET) String street,
            @NotBlank(message = "Number is required") @QueryParam(RequestParams.NUMBER) String number,
            @QueryParam(RequestParams.COMPLEMENT) String complement) {

        Address address = this.addressController.getAmericanAddressByZipCode(zipCode, street, number, complement);

        return Response.status(Response.Status.OK)
                .entity(new AddressResponseDto(address))
                .build();
    }

    @GET
    @Path("/pt/{zipCode}")
    @PermitAll
    @Operation(
            summary     = "Lookup Portuguese address by postal code",
            description = "Fetches city and district from Zippopotam API. Street and number must be provided manually."
    )
    @APIResponse(
            responseCode = "200",
            description  = "Address found",
            content      = @Content(
                    mediaType = "application/json",
                    examples  = @ExampleObject(name = "Success", value = AddressExamples.PT_RESPONSE)
            )
    )
    @APIResponse(responseCode = "400", description = "Missing required parameters or invalid postal code")
    @APIResponse(responseCode = "404", description = "ZIP code not found")
    @APIResponse(responseCode = "503", description = "Zippopotam unavailable")
    public Response getPortugueseAddressByZipCode(
            @PathParam(RequestParams.ZIP_CODE)    String zipCode,
            @NotBlank(message = "Street is required") @QueryParam(RequestParams.STREET) String street,
            @NotBlank(message = "Number is required") @QueryParam(RequestParams.NUMBER) String number,
            @QueryParam(RequestParams.COMPLEMENT) String complement) {

        Address address = this.addressController.getPortugueseAddressByZipCode(zipCode, street, number, complement);

        return Response.status(Response.Status.OK)
                .entity(new AddressResponseDto(address))
                .build();
    }
}
