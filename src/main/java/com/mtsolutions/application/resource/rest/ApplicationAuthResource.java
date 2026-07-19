package com.mtsolutions.application.resource.rest;

import com.mtsolutions.application.common.RequestParams;
import com.mtsolutions.application.resource.rest.examples.ApplicationAuthExamples;
import com.mtsolutions.domain.controller.ApplicationAuthController;
import com.mtsolutions.domain.dto.request.GenerateOwnerTokenRequestDto;
import com.mtsolutions.domain.dto.request.GenerateUserTokenRequestDto;
import com.mtsolutions.domain.dto.response.AppTokenResponseDto;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@RequestScoped
@Path("/api/v1/auth/application")
@Tag(name = "Application Auth", description = "Application Auth API")
public class ApplicationAuthResource {

    private final ApplicationAuthController applicationAuthController;

    public ApplicationAuthResource(ApplicationAuthController applicationAuthController) {
        this.applicationAuthController = applicationAuthController;
    }

    @POST
    @Path("/token")
    @PermitAll
    @Operation(
            summary = "Generate application token",
            description = "Authenticates an application using apiKey and apiSecret headers and returns a JWT token."
    )
    @APIResponse(
            responseCode = "200",
            description = "Application token generated successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Application token response",
                            value = ApplicationAuthExamples.APP_TOKEN_RESPONSE)
            )
    )
    public Response generateApplicationToken(@HeaderParam(RequestParams.API_KEY) String apiKey,
                                             @HeaderParam(RequestParams.API_SECRET) String apiSecret) {
        AppTokenResponseDto response = this.applicationAuthController.generateApplicationToken(apiKey, apiSecret);

        return Response.status(Response.Status.OK)
                .entity(response)
                .build();
    }
}
