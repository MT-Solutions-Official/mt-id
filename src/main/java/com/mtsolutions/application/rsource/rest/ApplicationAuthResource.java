package com.mtsolutions.application.rsource.rest;

import com.mtsolutions.application.rsource.rest.examples.ApplicationAuthExamples;
import com.mtsolutions.domain.controller.ApplicationAuthController;
import com.mtsolutions.domain.dto.request.GenerateOwnerTokenRequestDto;
import com.mtsolutions.domain.dto.response.AppTokenResponseDto;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
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
@Path("/api/v1/auth")
@Tag(name = "Application Auth", description = "Application Auth API")
public class ApplicationAuthResource {

    private final ApplicationAuthController applicationAuthController;

    public ApplicationAuthResource(ApplicationAuthController applicationAuthController) {
        this.applicationAuthController = applicationAuthController;
    }

    @POST
    @Path("/owner-token")
    @PermitAll
    @Operation(
            summary = "Generate owner token",
            description = "Authenticates an owner using email and password and returns a JWT token."
    )
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Owner token request",
                            value = ApplicationAuthExamples.OWNER_TOKEN_REQUEST)
            )
    )
    @APIResponse(
            responseCode = "200",
            description = "Owner token generated successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Owner token response",
                            value = ApplicationAuthExamples.OWNER_TOKEN_RESPONSE)
            )
    )
    public Response generateOwnerToken(GenerateOwnerTokenRequestDto request) {
        AppTokenResponseDto response = this.applicationAuthController.generateOwnerToken(request.email(), request.password());

        return Response.status(Response.Status.OK)
                .entity(response)
                .build();
    }
}
