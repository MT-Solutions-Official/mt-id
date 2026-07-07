package com.mtsolutions.application.client.kodepos;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "kodepos")
public interface KodePosClient {

    @GET
    @Path("/search/")
    KodePosResponseDto getAddress(@QueryParam("q") String zipCode);
}
