package com.mtsolutions.application.rsource.rest;

import com.mtsolutions.application.common.RequestParams;
import com.mtsolutions.application.rsource.rest.examples.OwnerRoleExamples;
import com.mtsolutions.domain.controller.OwnerRoleController;
import com.mtsolutions.domain.dto.request.CreateOwnerRoleRequestDto;
import com.mtsolutions.domain.dto.request.UpdateOwnerRoleRequestDto;
import com.mtsolutions.domain.entity.OwnerRole;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@RequestScoped
@Path("/api/v1/owner-roles")
@Tag(name = "Owner Role", description = "Owner Role API")
public class OwnerRoleResource {

    private final OwnerRoleController ownerRoleController;

    public OwnerRoleResource(OwnerRoleController ownerRoleController) {
        this.ownerRoleController = ownerRoleController;
    }

    @POST
    @Path("/create")
    @RolesAllowed("ADMIN")
    @Operation(
            summary = "Create a new owner role",
            description = "Creates a new role for application owners."
    )
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Create Owner Role",
                            value = OwnerRoleExamples.CREATE_OWNER_ROLE)
            )

    )
    @APIResponse(
            responseCode = "200",
            description = "Owner role created successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Owner Role Created",
                            value = OwnerRoleExamples.OWNER_ROLE_CREATED)
            )
    )
    public Response create(@Valid CreateOwnerRoleRequestDto request) {
        OwnerRole ownerRole = this.ownerRoleController.createOwnerRole(request);

        return Response.status(Response.Status.CREATED)
                .entity(ownerRole)
                .build();
    }

    @GET
    @Path("/{ownerRoleId}")
    @RolesAllowed("ADMIN")
    @Operation(
            summary = "Find owner role by ID",
            description = "Finds an owner role by ID."
    )
    @APIResponse(
            responseCode = "200",
            description = "Owner role found successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Owner Role Found",
                            value = OwnerRoleExamples.OWNER_ROLE_CREATED)
            )
    )
    public Response findById(@PathParam(RequestParams.OWNER_ROLE_ID) String ownerRoleId) {
        OwnerRole ownerRole = this.ownerRoleController.findOwnerRoleById(ownerRoleId);

        return Response.status(Response.Status.OK)
                .entity(ownerRole)
                .build();
    }

    @GET
    @RolesAllowed("ADMIN")
    @Operation(
            summary = "Find all owner roles",
            description = "Finds all owner roles."
    )
    @APIResponse(
            responseCode = "200",
            description = "Owner roles found successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Owner Role List",
                            value = OwnerRoleExamples.OWNER_ROLE_LIST)
            )
    )
    public Response findAll() {
        List<OwnerRole> ownerRoles = this.ownerRoleController.findAllOwnerRoles();

        return Response.status(Response.Status.OK)
                .entity(ownerRoles)
                .build();
    }

    @PATCH
    @Path("/update")
    @RolesAllowed("ADMIN")
    @Operation(
            summary = "Update owner role",
            description = "Updates an owner role name."
    )
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Update Owner Role",
                            value = OwnerRoleExamples.UPDATE_OWNER_ROLE)
            )

    )
    @APIResponse(
            responseCode = "200",
            description = "Owner role updated successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Owner Role Updated",
                            value = OwnerRoleExamples.OWNER_ROLE_UPDATED)
            )
    )
    public Response update(@Valid UpdateOwnerRoleRequestDto request) {
        OwnerRole ownerRole = this.ownerRoleController.updateOwnerRole(request);

        return Response.status(Response.Status.OK)
                .entity(ownerRole)
                .build();
    }

    @DELETE
    @Path("/{ownerRoleId}")
    @RolesAllowed("ADMIN")
    @Operation(
            summary = "Delete owner role",
            description = "Deletes an owner role by ID."
    )
    @APIResponse(
            responseCode = "204",
            description = "Owner role deleted successfully"
    )
    public Response delete(@PathParam(RequestParams.OWNER_ROLE_ID) String ownerRoleId) {
        this.ownerRoleController.deleteOwnerRole(ownerRoleId);

        return Response.status(Response.Status.NO_CONTENT)
                .build();
    }
}
