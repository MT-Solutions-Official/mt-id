package com.mtsolutions.application.client.hibp;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "pwned-passwords")
@ClientHeaderParam(name = "User-Agent", value = "mt-id")
@ClientHeaderParam(name = "Add-Padding", value = "true")
public interface PwnedPasswordsClient {

    @GET
    @Path("/range/{prefix}")
    @Produces(MediaType.TEXT_PLAIN)
    String range(@PathParam("prefix") String prefix);
}
