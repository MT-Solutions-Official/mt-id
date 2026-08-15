package com.mtsolutions.application.resource.rest;

import com.mtsolutions.application.resource.rest.examples.ClientApplicationExamples;
import com.mtsolutions.domain.controller.ClientApplicationController;
import com.mtsolutions.domain.dto.request.AddOwnersToClientApplicationRequestDto;
import com.mtsolutions.domain.dto.response.ClientApplicationResponseDto;
import com.mtsolutions.domain.dto.request.CreateClientApplicationRequestDto;
import com.mtsolutions.domain.dto.request.UpdateClientApplicationSettingsRequestDto;
import com.mtsolutions.domain.dto.request.UpdateRequiredUserFieldsRequestDto;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import io.quarkus.security.Authenticated;

@RequestScoped
@Path("/api/v1/client-applications")
@Tag(name = "Client Application", description = "Client Application API")
public class ClientApplicationResource {

    private final ClientApplicationController applicationController;

    public ClientApplicationResource(ClientApplicationController applicationController) {
        this.applicationController = applicationController;
    }

    @POST
    @Path("/create")
    @RolesAllowed("OWNER_WRITER")
    @Operation(
            summary = "Create a new client application",
            description = "Creates a new client application with the provided details."
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
            responseCode = "200",
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
            description = "Adds owners to a client application with the provided details."
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
    public Response addOwnersToClientApplication(AddOwnersToClientApplicationRequestDto request) {
        this.applicationController.addOwnersToClientApplication(request);

        return Response.status(Response.Status.NO_CONTENT)
                .build();
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
            description = "Updates allowed origins, Google audience and token TTLs for a client application."
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
                            value = ClientApplicationExamples.CLIENT_APPLICATION_CREATED)
            )
    )
    public Response updateSettings(UpdateClientApplicationSettingsRequestDto request) {
        return Response.ok(new ClientApplicationResponseDto(this.applicationController.updateSettings(request)))
                .build();
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
