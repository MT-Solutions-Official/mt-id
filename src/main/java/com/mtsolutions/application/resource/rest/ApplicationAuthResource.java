package com.mtsolutions.application.resource.rest;

import com.mtsolutions.application.common.RequestParams;
import com.mtsolutions.application.resource.rest.examples.ApplicationAuthExamples;
import com.mtsolutions.domain.controller.ApplicationAuthController;
import com.mtsolutions.domain.dto.request.GenerateOwnerTokenRequestDto;
import com.mtsolutions.domain.dto.request.GenerateUserTokenRequestDto;
import com.mtsolutions.domain.dto.response.AppTokenResponseDto;
import com.mtsolutions.domain.dto.response.OwnerTokenResponseDto;
import com.mtsolutions.domain.dto.response.UserTokenResponseDto;
import io.quarkus.security.Authenticated;
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
        OwnerTokenResponseDto response = this.applicationAuthController.generateOwnerToken(request.email(), request.password());

        return Response.status(Response.Status.OK)
                .entity(response)
                .build();
    }

    @POST
    @Path("/app-token")
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

    @POST
    @Path("/user-token")
    @PermitAll
    @Operation(
            summary = "Generate user token",
            description = "Authenticates a user using email and password and returns a JWT token."
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
        UserTokenResponseDto response = this.applicationAuthController.generateUserToken(request.email(), request.password());

        return Response.status(Response.Status.OK)
                .entity(response)
                .build();
    }

    @POST
    @Path("/owner-token/refresh")
    @Authenticated
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
        OwnerTokenResponseDto response = this.applicationAuthController.refreshOwnerToken();

        return Response.status(Response.Status.OK)
                .entity(response)
                .build();
    }

    @POST
    @Path("/user-token/refresh")
    @Authenticated
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
        UserTokenResponseDto response = this.applicationAuthController.refreshUserToken();

        return Response.status(Response.Status.OK)
                .entity(response)
                .build();
    }

    @POST
    @Path("/owner-token/logout")
    @Authenticated
    @Operation(
            summary = "Logout owner",
            description = "Revokes the current owner refresh token."
    )
    @APIResponse(
            responseCode = "204",
            description = "Owner logged out successfully"
    )
    public Response logoutOwner() {
        this.applicationAuthController.logoutOwner();
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @POST
    @Path("/user-token/logout")
    @Authenticated
    @Operation(
            summary = "Logout user",
            description = "Revokes the current user refresh token."
    )
    @APIResponse(
            responseCode = "204",
            description = "User logged out successfully"
    )
    public Response logoutUser() {
        this.applicationAuthController.logoutUser();
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
