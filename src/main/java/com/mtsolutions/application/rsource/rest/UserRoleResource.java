package com.mtsolutions.application.rsource.rest;

import com.mtsolutions.application.rsource.rest.examples.UserRoleExamples;
import com.mtsolutions.domain.controller.UserRoleController;
import com.mtsolutions.domain.dto.request.CreateUserRoleRequestDto;
import com.mtsolutions.domain.entity.UserRole;
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
@Path("/api/v1/user-roles")
@Tag(name = "User Role", description = "User Role API")
public class UserRoleResource {

    private final UserRoleController userRoleController;

    public UserRoleResource(UserRoleController userRoleController) {
        this.userRoleController = userRoleController;
    }

    @POST
    @Path("/create")
    @PermitAll
    @Operation(
            summary = "Create a new user role",
            description = "Creates a new role for a specific client application."
    )
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Create User Role",
                            value = UserRoleExamples.CREATE_USER_ROLE)
            )

    )
    @APIResponse(
            responseCode = "200",
            description = "User role created successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "User Role Created",
                            value = UserRoleExamples.USER_ROLE_CREATED)
            )
    )
    public Response create(CreateUserRoleRequestDto request) {
        UserRole userRole = this.userRoleController.createUserRole(request);

        return Response.status(Response.Status.CREATED)
                .entity(userRole)
                .build();
    }
}
