package com.mtsolutions.application.rsource.rest;

import com.mtsolutions.application.common.RequestParams;
import com.mtsolutions.application.rsource.rest.examples.UserRoleExamples;
import com.mtsolutions.application.exception.ApplicationForbiddenException;
import com.mtsolutions.domain.controller.UserRoleController;
import com.mtsolutions.domain.dto.request.CreateUserRoleRequestDto;
import com.mtsolutions.domain.dto.request.UpdateUserRoleRequestDto;
import com.mtsolutions.domain.entity.UserRole;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import io.quarkus.security.Authenticated;

import java.util.List;

@RequestScoped
@Path("/api/v1/user-roles")
@Tag(name = "User Role", description = "User Role API")
public class UserRoleResource {

    private final UserRoleController userRoleController;
    private final JsonWebToken jwt;

    public UserRoleResource(UserRoleController userRoleController, JsonWebToken jwt) {
        this.userRoleController = userRoleController;
        this.jwt = jwt;
    }

    @POST
    @Path("/create")
    @Authenticated
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
        UserRole userRole = this.userRoleController.createUserRole(request, this.getAuthenticatedAppId());

        return Response.status(Response.Status.CREATED)
                .entity(userRole)
                .build();
    }

    @GET
    @Path("/{userRoleId}")
    @Authenticated
    @Operation(
            summary = "Find user role by ID",
            description = "Finds a user role by ID."
    )
    @APIResponse(
            responseCode = "200",
            description = "User role found successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "User Role Found",
                            value = UserRoleExamples.USER_ROLE_CREATED)
            )
    )
    public Response findById(@PathParam(RequestParams.USER_ROLE_ID) String userRoleId) {
        UserRole userRole = this.userRoleController.findUserRoleById(userRoleId, this.getAuthenticatedAppId());

        return Response.status(Response.Status.OK)
                .entity(userRole)
                .build();
    }

    @GET
    @Path("/app/{appId}")
    @Authenticated
    @Operation(
            summary = "Find user roles by application",
            description = "Finds all user roles for a specific application."
    )
    @APIResponse(
            responseCode = "200",
            description = "User roles found successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "User Role List",
                            value = UserRoleExamples.USER_ROLE_LIST)
            )
    )
    public Response findByAppId(@PathParam(RequestParams.APP_ID) String appId) {
        List<UserRole> userRoles = this.userRoleController.findUserRolesByAppId(appId, this.getAuthenticatedAppId());

        return Response.status(Response.Status.OK)
                .entity(userRoles)
                .build();
    }

    @PATCH
    @Path("/update")
    @Authenticated
    @Operation(
            summary = "Update user role",
            description = "Updates a user role name."
    )
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Update User Role",
                            value = UserRoleExamples.UPDATE_USER_ROLE)
            )

    )
    @APIResponse(
            responseCode = "200",
            description = "User role updated successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "User Role Updated",
                            value = UserRoleExamples.USER_ROLE_UPDATED)
            )
    )
    public Response update(UpdateUserRoleRequestDto request) {
        UserRole userRole = this.userRoleController.updateUserRole(request, this.getAuthenticatedAppId());

        return Response.status(Response.Status.OK)
                .entity(userRole)
                .build();
    }

    @DELETE
    @Path("/{userRoleId}")
    @Authenticated
    @Operation(
            summary = "Delete user role",
            description = "Deletes a user role by ID."
    )
    @APIResponse(
            responseCode = "204",
            description = "User role deleted successfully"
    )
    public Response delete(@PathParam(RequestParams.USER_ROLE_ID) String userRoleId) {
        this.userRoleController.deleteUserRole(userRoleId, this.getAuthenticatedAppId());

        return Response.status(Response.Status.NO_CONTENT)
                .build();
    }

    private String getAuthenticatedAppId() {
        String appId = this.jwt.getClaim("app_id");
        if (appId == null || appId.isBlank()) {
            throw new ApplicationForbiddenException();
        }

        return appId;
    }
}
