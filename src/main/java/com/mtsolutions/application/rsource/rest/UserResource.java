package com.mtsolutions.application.rsource.rest;

import com.mtsolutions.application.rsource.rest.examples.UserExamples;
import com.mtsolutions.domain.controller.UserController;
import com.mtsolutions.domain.dto.request.CreateUserRequestDto;
import com.mtsolutions.domain.dto.response.UserResponseDto;
import com.mtsolutions.domain.entity.User;
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
@Path("/api/v1/users")
@Tag(name = "User", description = "User API")
public class UserResource {

    private final UserController userController;

    public UserResource(UserController userController) {
        this.userController = userController;
    }

    @POST
    @Path("/create")
    @PermitAll
    @Operation(
            summary = "Create a new user",
            description = "Creates a new user with the provided details."
    )
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Create User",
                            value = UserExamples.CREATE_USER)
            )

    )
    @APIResponse(
            responseCode = "200",
            description = "User created successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "User Created",
                            value = UserExamples.USER_CREATED)
            )
    )
    public Response create(CreateUserRequestDto request) {
        User user = this.userController.createUser(request);

        return Response.status(Response.Status.CREATED)
                .entity(new UserResponseDto(user))
                .build();
    }
}
