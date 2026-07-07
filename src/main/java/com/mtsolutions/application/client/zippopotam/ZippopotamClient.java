package com.mtsolutions.application.client.zippopotam;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "zippopotam")
public interface ZippopotamClient {

    @GET
    @Path("/{countryCode}/{zipCode}")
    ZippopotamResponseDto getAddress(
            @PathParam("countryCode") String countryCode,
            @PathParam("zipCode") String zipCode
    );
}
