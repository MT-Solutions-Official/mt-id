package com.mtsolutions.application.resource.rest;

import com.mtsolutions.application.resource.rest.examples.OwnerExamples;
import com.mtsolutions.domain.controller.OwnerController;
import com.mtsolutions.domain.dto.request.CreateOwnerRequestDto;
import com.mtsolutions.domain.entity.Owner;
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
@Path("/api/v1/owner")
@Tag(name = "Owner", description = "Owner API")
public class OwnerResource {

    private final OwnerController ownerController;

    public OwnerResource(OwnerController ownerController) {
        this.ownerController = ownerController;
    }

    @POST
    @Path("/create")
    @PermitAll
    @Operation(
            summary = "Create a new owner",
            description = "Creates a new owner with the provided details."
    )
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Create Owner",
                            value = OwnerExamples.CREATE_OWNER)
            )

    )
    @APIResponse(
            responseCode = "200",
            description = "Owner created successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Owner Created",
                            value = OwnerExamples.OWNER_CREATED)
            )
    )
    public Response create(CreateOwnerRequestDto request) {
        Owner owner = this.ownerController.createOwner(request);

        return Response.status(Response.Status.CREATED)
                .entity(owner)
                .build();
    }
}
