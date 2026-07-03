package com.mtsolutions.application.rsource.rest;

import com.mtsolutions.application.rsource.rest.examples.ClientApplicationExamples;
import com.mtsolutions.domain.controller.ClientApplicationController;
import com.mtsolutions.domain.dto.AddOwnersToClientApplicationRequestDto;
import com.mtsolutions.domain.dto.ClientApplicationResponseDto;
import com.mtsolutions.domain.dto.CreateClientApplicationRequestDto;
import com.mtsolutions.domain.dto.UpdateRequiredUserFieldsRequestDto;
import com.mtsolutions.domain.entity.ClientApplication;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.PATCH;
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
@Path("/api/v1/client-applications")
@Tag(name = "Client Application", description = "Client Application API")
public class ClientApplicationResource {

    private final ClientApplicationController applicationController;

    public ClientApplicationResource(ClientApplicationController applicationController) {
        this.applicationController = applicationController;
    }

    @POST
    @Path("/create")
    @PermitAll
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
        ClientApplication application = this.applicationController.createClientApplication(request);

        return Response.status(Response.Status.CREATED)
                .entity(new ClientApplicationResponseDto(application))
                .build();
    }

    @PATCH
    @Path("/add-owner")
    @PermitAll
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
    @PermitAll
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
}
