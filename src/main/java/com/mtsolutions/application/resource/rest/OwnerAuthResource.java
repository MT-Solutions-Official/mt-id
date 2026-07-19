package com.mtsolutions.application.resource.rest;

import com.mtsolutions.application.resource.rest.examples.ApplicationAuthExamples;
import com.mtsolutions.domain.controller.OwnerAuthController;
import com.mtsolutions.domain.dto.request.GenerateGoogleTokenRequestDto;
import com.mtsolutions.domain.dto.request.GenerateOwnerTokenRequestDto;
import com.mtsolutions.domain.dto.response.OwnerTokenResponseDto;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
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
@Path("/api/v1/auth/owners")
@Tag(name = "Owner Auth", description = "Owner Auth API")
public class OwnerAuthResource {

    private final OwnerAuthController ownerAuthController;

    public OwnerAuthResource(OwnerAuthController ownerAuthController) {
        this.ownerAuthController = ownerAuthController;
    }

    @POST
    @Path("/token")
    @PermitAll
    @Operation(
            summary = "Generate owner token",
            description = "Authenticates an owner using email and password and returns MT-ID JWT tokens."
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
        OwnerTokenResponseDto response = this.ownerAuthController.generateOwnerToken(request.email(), request.password());
        return Response.status(Response.Status.OK)
                .entity(response)
                .build();
    }

    @POST
    @Path("/google-token")
    @PermitAll
    @Operation(
            summary = "Generate owner token with Google",
            description = "Authenticates an owner using a Google ID token and returns MT-ID JWT tokens."
    )
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Owner Google token request",
                            value = ApplicationAuthExamples.GOOGLE_TOKEN_REQUEST)
            )
    )
    @APIResponse(
            responseCode = "200",
            description = "Owner Google token generated successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Owner Google token response",
                            value = ApplicationAuthExamples.OWNER_TOKEN_RESPONSE)
            )
    )
    public Response generateGoogleOwnerToken(GenerateGoogleTokenRequestDto request) {
        OwnerTokenResponseDto response = this.ownerAuthController.generateGoogleOwnerToken(request.idToken());
        return Response.status(Response.Status.OK)
                .entity(response)
                .build();
    }

    @POST
    @Path("/refresh")
    @RolesAllowed("REFRESH_TOKEN")
    @Operation(
            summary = "Refresh owner token",
            description = "Uses a valid owner refresh token to rotate the owner access and refresh tokens."
    )
    @APIResponse(
            responseCode = "200",
            description = "Owner token refreshed successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Owner token refresh response",
                            value = ApplicationAuthExamples.OWNER_TOKEN_REFRESH_RESPONSE)
            )
    )
    public Response refreshOwnerToken() {
        return Response.status(Response.Status.OK)
                .entity(this.ownerAuthController.refreshOwnerToken())
                .build();
    }

    @POST
    @Path("/logout")
    @RolesAllowed("REFRESH_TOKEN")
    @Operation(
            summary = "Logout owner",
            description = "Revokes the current owner refresh token."
    )
    @APIResponse(
            responseCode = "204",
            description = "Owner logged out successfully"
    )
    public Response logoutOwner() {
        this.ownerAuthController.logoutOwner();
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
