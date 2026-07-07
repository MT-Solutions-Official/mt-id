package com.mtsolutions.application.client.viacep;

import com.mtsolutions.application.common.RequestParams;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "viacep")
public interface ViaCepClient {


    @GET
    @Path("/ws/{cep}/json/")
    ViaCepResponseDto getAddress(@PathParam(RequestParams.CEP) String cep);

}
