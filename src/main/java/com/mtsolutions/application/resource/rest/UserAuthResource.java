package com.mtsolutions.application.resource.rest;

import com.mtsolutions.application.resource.rest.examples.ApplicationAuthExamples;
import com.mtsolutions.domain.controller.UserAuthController;
import com.mtsolutions.domain.dto.request.GenerateGoogleTokenRequestDto;
import com.mtsolutions.domain.dto.request.GenerateUserTokenRequestDto;
import com.mtsolutions.domain.dto.response.UserTokenResponseDto;
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
@Path("/api/v1/auth/users")
@Tag(name = "User Auth", description = "User Auth API")
public class UserAuthResource {

    private final UserAuthController userAuthController;

    public UserAuthResource(UserAuthController userAuthController) {
        this.userAuthController = userAuthController;
    }

    @POST
    @Path("/token")
    @PermitAll
    @Operation(
            summary = "Generate user token",
            description = "Authenticates a user using email and password and returns MT-ID JWT tokens."
    )
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "User token request",
                            value = ApplicationAuthExamples.USER_TOKEN_REQUEST)
            )
    )
    @APIResponse(
            responseCode = "200",
            description = "User token generated successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "User token response",
                            value = ApplicationAuthExamples.USER_TOKEN_RESPONSE)
            )
    )
    public Response generateUserToken(GenerateUserTokenRequestDto request) {
        UserTokenResponseDto response = this.userAuthController.generateUserToken(request.email(), request.password());
        return Response.status(Response.Status.OK)
                .entity(response)
                .build();
    }

    @POST
    @Path("/google-token")
    @PermitAll
    @Operation(
            summary = "Generate user token with Google",
            description = "Authenticates a user using a Google ID token and returns MT-ID JWT tokens."
    )
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "User Google token request",
                            value = ApplicationAuthExamples.GOOGLE_TOKEN_REQUEST)
            )
    )
    @APIResponse(
            responseCode = "200",
            description = "User Google token generated successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "User Google token response",
                            value = ApplicationAuthExamples.USER_TOKEN_RESPONSE)
            )
    )
    public Response generateGoogleUserToken(GenerateGoogleTokenRequestDto request) {
        UserTokenResponseDto response = this.userAuthController.generateGoogleUserToken(request.idToken());
        return Response.status(Response.Status.OK)
                .entity(response)
                .build();
    }

    @POST
    @Path("/refresh")
    @RolesAllowed("REFRESH_TOKEN")
    @Operation(
            summary = "Refresh user token",
            description = "Uses a valid user refresh token to rotate the user access and refresh tokens."
    )
    @APIResponse(
            responseCode = "200",
            description = "User token refreshed successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "User token refresh response",
                            value = ApplicationAuthExamples.USER_TOKEN_REFRESH_RESPONSE)
            )
    )
    public Response refreshUserToken() {
        return Response.status(Response.Status.OK)
                .entity(this.userAuthController.refreshUserToken())
                .build();
    }

    @POST
    @Path("/logout")
    @RolesAllowed("REFRESH_TOKEN")
    @Operation(
            summary = "Logout user",
            description = "Revokes the current user refresh token."
    )
    @APIResponse(
            responseCode = "204",
            description = "User logged out successfully"
    )
    public Response logoutUser() {
        this.userAuthController.logoutUser();
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
