package com.mtsolutions.application.resource.rest;

import com.mtsolutions.application.common.RequestParams;
import com.mtsolutions.application.resource.rest.examples.UserExamples;
import com.mtsolutions.domain.controller.UserController;
import com.mtsolutions.domain.constant.ImageType;
import com.mtsolutions.domain.dto.request.CreateAddressRequestDto;
import com.mtsolutions.domain.dto.request.CreateUserRequestDto;
import com.mtsolutions.domain.dto.request.RemoveUserImageRequestDto;
import com.mtsolutions.domain.dto.request.UploadUserImageRequestDto;
import com.mtsolutions.domain.dto.response.UserResponseDto;
import com.mtsolutions.domain.entity.User;
import jakarta.enterprise.context.RequestScoped;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;
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
    @RolesAllowed("APPLICATION")
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

    @PATCH
    @Path("/{userId}/address")
    @RolesAllowed({"APPLICATION", "USER"})
    @Operation(
            summary = "Attach address to user",
            description = "Appends an address to the user. Common fields are required and country-specific fields are optional."
    )
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = {
                            @ExampleObject(
                                    name = "Attach Address To User BR",
                                    value = UserExamples.ATTACH_ADDRESS_TO_USER_BR
                            ),
                            @ExampleObject(
                                    name = "Attach Address To User ID",
                                    value = UserExamples.ATTACH_ADDRESS_TO_USER_ID
                            ),
                            @ExampleObject(
                                    name = "Attach Address To User US",
                                    value = UserExamples.ATTACH_ADDRESS_TO_USER_US
                            ),
                            @ExampleObject(
                                    name = "Attach Address To User PT",
                                    value = UserExamples.ATTACH_ADDRESS_TO_USER_PT
                            )
                    }
            )
    )
    @APIResponse(
            responseCode = "200",
            description = "Address attached successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "User With Address",
                            value = UserExamples.USER_WITH_ADDRESS)
            )
    )
    public Response attachAddress(@PathParam(RequestParams.USER_ID) String userId,
                                  @Valid CreateAddressRequestDto request) {
        User user = this.userController.attachAddressToUser(userId, request);

        return Response.status(Response.Status.OK)
                .entity(new UserResponseDto(user))
                .build();
    }

    @DELETE
    @Path("/{userId}/address/{addressIndex}")
    @RolesAllowed({"APPLICATION", "USER"})
    @Operation(
            summary = "Remove address from user",
            description = "Removes an address from the user's address list by index."
    )
    @APIResponse(
            responseCode = "204",
            description = "Address removed successfully"
    )
    public Response removeAddress(@PathParam(RequestParams.USER_ID) String userId,
                                  @PathParam(RequestParams.ADDRESS_INDEX) Integer addressIndex) {
        this.userController.removeAddressFromUser(userId, addressIndex);

        return Response.status(Response.Status.NO_CONTENT)
                .build();
    }

    @POST
    @Path("/{userId}/images/{imageType}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({"APPLICATION", "USER"})
    @Operation(
            summary = "Upload user image",
            description = "Uploads an image to Cloudinary and stores the reference on the user."
    )
    @APIResponse(
            responseCode = "200",
            description = "Image uploaded successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "User With Uploaded Image",
                            value = UserExamples.USER_WITH_IMAGE)
            )
    )
    public Response uploadImage(@PathParam(RequestParams.USER_ID) String userId,
                                @PathParam(RequestParams.IMAGE_TYPE) ImageType imageType,
                                @RestForm("image") FileUpload image) {
        User user = this.userController.uploadUserImage(new UploadUserImageRequestDto(
                userId,
                imageType,
                image.uploadedFile(),
                image.fileName(),
                image.size(),
                image.contentType()
        ));

        return Response.status(Response.Status.OK)
                .entity(new UserResponseDto(user))
                .build();
    }

    @DELETE
    @Path("/{userId}/images/{imageType}")
    @RolesAllowed({"APPLICATION", "USER"})
    @Operation(
            summary = "Remove user image",
            description = "Removes the image of the given type from Cloudinary and from the user record."
    )
    @APIResponse(
            responseCode = "204",
            description = "Image removed successfully"
    )
    public Response removeImage(@PathParam(RequestParams.USER_ID) String userId,
                                @PathParam(RequestParams.IMAGE_TYPE) ImageType imageType) {
        this.userController.removeUserImage(new RemoveUserImageRequestDto(userId, imageType));

        return Response.status(Response.Status.NO_CONTENT)
                .build();
    }
}
