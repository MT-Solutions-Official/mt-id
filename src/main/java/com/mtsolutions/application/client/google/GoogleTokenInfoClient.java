package com.mtsolutions.application.client.google;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "google-tokeninfo")
public interface GoogleTokenInfoClient {

    @GET
    @Path("/tokeninfo")
    GoogleTokenInfoResponseDto verifyIdToken(@QueryParam("id_token") String idToken);
}
