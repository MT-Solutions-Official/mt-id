package com.mtsolutions.application.resource.rest;

import com.mtsolutions.application.common.RequestParams;
import com.mtsolutions.application.resource.rest.examples.ClientApplicationExamples;
import com.mtsolutions.domain.controller.ClientApplicationController;
import com.mtsolutions.domain.dto.request.AddOwnersToClientApplicationRequestDto;
import com.mtsolutions.domain.dto.response.ClientApplicationResponseDto;
import com.mtsolutions.domain.dto.request.CreateClientApplicationRequestDto;
import com.mtsolutions.domain.dto.request.UpdateClientApplicationSettingsRequestDto;
import com.mtsolutions.domain.dto.request.UpdateRequiredUserFieldsRequestDto;
import jakarta.enterprise.context.RequestScoped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@RequestScoped
@Path("/api/v1/client-applications")
@Tag(name = "Client Application", description = "Client Application API")
public class ClientApplicationResource {

    private final ClientApplicationController applicationController;

    public ClientApplicationResource(ClientApplicationController applicationController) {
        this.applicationController = applicationController;
    }

    @GET
    @RolesAllowed({"OWNER_WRITER", "OWNER_VIEWER"})
    @Operation(
            summary = "List my client applications",
            description = "Returns the client applications owned by the authenticated owner. The API secret is never returned."
    )
    @APIResponse(
            responseCode = "200",
            description = "Client applications",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Client applications",
                            value = ClientApplicationExamples.CLIENT_APPLICATION_LIST)
            )
    )
    public Response listMine() {
        return Response.ok(
                this.applicationController.listMyApplications().stream()
                        .map(ClientApplicationResponseDto::new)
                        .toList()
        ).build();
    }

    @GET
    @Path("/{appId}")
    @RolesAllowed({"OWNER_WRITER", "OWNER_VIEWER"})
    @Operation(
            summary = "Get a client application",
            description = "Returns a client application owned by the authenticated owner. The API secret is never returned."
    )
    @APIResponse(
            responseCode = "200",
            description = "Client application",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Client application",
                            value = ClientApplicationExamples.CLIENT_APPLICATION)
            )
    )
    public Response findById(@PathParam(RequestParams.APP_ID) @NotBlank String appId) {
        return Response.ok(new ClientApplicationResponseDto(this.applicationController.findOwnedClientApplication(appId)))
                .build();
    }

    @POST
    @Path("/create")
    @RolesAllowed("OWNER_WRITER")
    @Operation(
            summary = "Create a new client application",
            description = "Creates a new client application with the provided details. The API secret is returned only once."
    )
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Create Client Application",
                            value = ClientApplicationExamples.CREATE_CLIENT_APPLICATION)
            )

    )
    @APIResponse(
            responseCode = "201",
            description = "Client application created successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Client Application Created",
                            value = ClientApplicationExamples.CLIENT_APPLICATION_CREATED)
            )
    )
    public Response create(CreateClientApplicationRequestDto request) {
        var result = this.applicationController.createClientApplication(request);

        return Response.status(Response.Status.CREATED)
                .entity(new ClientApplicationResponseDto(result.clientApplication(), result.apiSecret()))
                .build();
    }

    @PATCH
    @Path("/add-owner")
    @RolesAllowed("OWNER_WRITER")
    @Operation(
            summary = "Add owners to a client application",
            description = "Adds owners by owner ID and/or email. The authenticated owner must already own the application."
    )
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Add Owners to Client Application",
                            value = ClientApplicationExamples.ADD_OWNERS_TO_CLIENT_APPLICATION)
            )

    )
    @APIResponse(
            responseCode = "204",
            description = "Owners added to client application successfully"
    )
    public Response addOwnersToClientApplication(@Valid AddOwnersToClientApplicationRequestDto request) {
        this.applicationController.addOwnersToClientApplication(request);

        return Response.status(Response.Status.NO_CONTENT)
                .build();
    }

    @DELETE
    @Path("/{appId}/owners/{ownerId}")
    @RolesAllowed("OWNER_WRITER")
    @Operation(
            summary = "Remove an owner from a client application",
            description = "Removes an owner from the application. The last owner cannot be removed."
    )
    @APIResponse(
            responseCode = "200",
            description = "Owner removed",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Client application",
                            value = ClientApplicationExamples.CLIENT_APPLICATION)
            )
    )
    public Response removeOwner(@PathParam(RequestParams.APP_ID) @NotBlank String appId,
                                @PathParam(RequestParams.OWNER_ID) @NotBlank String ownerId) {
        return Response.ok(new ClientApplicationResponseDto(
                this.applicationController.removeOwnerFromClientApplication(appId, ownerId)
        )).build();
    }

    @PATCH
    @Path("/required-user-fields")
    @RolesAllowed("OWNER_WRITER")
    @Operation(
            summary = "Update required user fields for a client application",
            description = "Defines which user fields must be present when creating users for the given client application."
    )
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Update required user fields",
                            value = ClientApplicationExamples.UPDATE_REQUIRED_USER_FIELDS)
            )

    )
    @APIResponse(
            responseCode = "204",
            description = "Required user fields updated successfully"
    )
    public Response updateRequiredUserFields(UpdateRequiredUserFieldsRequestDto request) {
        this.applicationController.updateRequiredUserFields(request);

        return Response.status(Response.Status.NO_CONTENT)
                .build();
    }

    @PATCH
    @Path("/settings")
    @RolesAllowed("OWNER_WRITER")
    @Operation(
            summary = "Update client application settings",
            description = "Updates branding, email settings, allowed origins, Google audience, required user fields and token TTLs. Only provided fields are changed. The API secret is never returned."
    )
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Update client application settings",
                            value = ClientApplicationExamples.UPDATE_CLIENT_APPLICATION_SETTINGS)
            )
    )
    @APIResponse(
            responseCode = "200",
            description = "Client application updated successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Client Application Updated",
                            value = ClientApplicationExamples.CLIENT_APPLICATION)
            )
    )
    public Response updateSettings(@Valid UpdateClientApplicationSettingsRequestDto request) {
        return Response.ok(new ClientApplicationResponseDto(this.applicationController.updateSettings(request)))
                .build();
    }

    @PATCH
    @Path("/{appId}/disable")
    @RolesAllowed("OWNER_WRITER")
    @Operation(
            summary = "Disable a client application",
            description = "Disables the application. Existing application access tokens are rejected on subsequent requests."
    )
    @APIResponse(
            responseCode = "200",
            description = "Client application disabled",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Disabled client application",
                            value = ClientApplicationExamples.CLIENT_APPLICATION)
            )
    )
    public Response disable(@PathParam(RequestParams.APP_ID) @NotBlank String appId) {
        return Response.ok(new ClientApplicationResponseDto(this.applicationController.disableClientApplication(appId)))
                .build();
    }

    @PATCH
    @Path("/{appId}/enable")
    @RolesAllowed("OWNER_WRITER")
    @Operation(
            summary = "Enable a client application",
            description = "Re-enables a previously disabled client application."
    )
    @APIResponse(
            responseCode = "200",
            description = "Client application enabled",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Enabled client application",
                            value = ClientApplicationExamples.CLIENT_APPLICATION)
            )
    )
    public Response enable(@PathParam(RequestParams.APP_ID) @NotBlank String appId) {
        return Response.ok(new ClientApplicationResponseDto(this.applicationController.enableClientApplication(appId)))
                .build();
    }

    @PATCH
    @Path("/{appId}/rotate-secret")
    @RolesAllowed("OWNER_WRITER")
    @Operation(
            summary = "Rotate API secret as owner",
            description = "Rotates the API secret of an owned client application and returns the new secret only once."
    )
    @APIResponse(
            responseCode = "200",
            description = "API secret rotated successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Client Application Secret Rotated",
                            value = ClientApplicationExamples.CLIENT_APPLICATION_SECRET_ROTATED)
            )
    )
    public Response rotateOwnedSecret(@PathParam(RequestParams.APP_ID) @NotBlank String appId) {
        var result = this.applicationController.rotateOwnedClientApplicationSecret(appId);
        return Response.ok(new ClientApplicationResponseDto(result.clientApplication(), result.apiSecret())).build();
    }

    @PATCH
    @Path("/rotate-secret")
    @RolesAllowed("APPLICATION")
    @Operation(
            summary = "Rotate API secret",
            description = "Rotates the authenticated client application's API secret and returns it only once."
    )
    @APIResponse(
            responseCode = "200",
            description = "API secret rotated successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Client Application Secret Rotated",
                            value = ClientApplicationExamples.CLIENT_APPLICATION_SECRET_ROTATED)
            )
    )
    public Response rotateSecret() {
        var result = this.applicationController.rotateClientApplicationSecret();

        return Response.status(Response.Status.OK)
                .entity(new ClientApplicationResponseDto(result.clientApplication(), result.apiSecret()))
                .build();
    }
}
