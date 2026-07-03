package com.mtsolutions.application.rsource.rest;

import com.mtsolutions.application.rsource.rest.examples.ApplicationOwnerExamples;
import com.mtsolutions.application.rsource.rest.examples.ClientApplicationExamples;
import com.mtsolutions.domain.controller.ApplicationOwnerController;
import com.mtsolutions.domain.dto.CreateApplicationOwnerRequestDto;
import com.mtsolutions.domain.entity.ApplicationOwner;
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
@Path("/api/v1/application-owner")
@Tag(name = "Application Owner", description = "Application Owner API")
public class ApplicationOwnerResource {

    private final ApplicationOwnerController applicationOwnerController;

    public ApplicationOwnerResource(ApplicationOwnerController applicationOwnerController) {
        this.applicationOwnerController = applicationOwnerController;
    }

    @POST
    @Path("/create")
    @PermitAll
    @Operation(
            summary = "Create a new application owner",
            description = "Creates a new application owner with the provided details."
    )
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Create Application Owner",
                            value = ApplicationOwnerExamples.CREATE_APPLICATION_OWNER)
            )

    )
    @APIResponse(
            responseCode = "200",
            description = "Application owner created successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Application Owner Created",
                            value = ApplicationOwnerExamples.APPLICATION_OWNER_CREATED)
            )
    )
    public Response create(CreateApplicationOwnerRequestDto request) {
        ApplicationOwner owner = this.applicationOwnerController.createApplicationOwner(request);

        return Response.status(Response.Status.CREATED)
                .entity(owner)
                .build();
    }
}
