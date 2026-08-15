package com.mtsolutions.application.resource.rest;

import com.mtsolutions.application.common.RequestParams;
import com.mtsolutions.application.resource.rest.examples.UserExamples;
import com.mtsolutions.domain.controller.UserController;
import com.mtsolutions.domain.constant.ImageType;
import com.mtsolutions.domain.dto.request.CreateAddressRequestDto;
import com.mtsolutions.domain.dto.request.CreateUserRequestDto;
import com.mtsolutions.domain.dto.request.ForgotPasswordRequestDto;
import com.mtsolutions.domain.dto.request.RemoveUserImageRequestDto;
import com.mtsolutions.domain.dto.request.ResetPasswordRequestDto;
import com.mtsolutions.domain.dto.request.SendEmailVerificationRequestDto;
import com.mtsolutions.domain.dto.request.UploadUserImageRequestDto;
import com.mtsolutions.domain.dto.response.UserResponseDto;
import com.mtsolutions.domain.entity.User;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@RequestScoped
@Path("/api/v1/users")
@Tag(name = "User", description = "User API")
public class UserResource {

    private final UserController userController;

    public UserResource(UserController userController) {
        this.userController = userController;
    }

    @GET
    @Path("/me")
    @RolesAllowed("USER")
    @Operation(
            summary = "Get the authenticated user",
            description = "Returns the profile of the user identified by the access token."
    )
    @APIResponse(
            responseCode = "200",
            description = "Current user",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Current user",
                            value = UserExamples.USER_CREATED)
            )
    )
    public Response me() {
        return Response.ok(new UserResponseDto(this.userController.findCurrentUser())).build();
    }

    @GET
    @Path("/{userId}")
    @RolesAllowed({"APPLICATION", "USER"})
    @Operation(
            summary = "Get user by ID",
            description = "Returns a user. An application can read users of its appId. A user can only read their own profile."
    )
    @APIResponse(
            responseCode = "200",
            description = "User",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "User",
                            value = UserExamples.USER_CREATED)
            )
    )
    public Response findById(@PathParam(RequestParams.USER_ID) String userId) {
        return Response.ok(new UserResponseDto(this.userController.findUserById(userId))).build();
    }

    @PATCH
    @Path("/{userId}/disable")
    @RolesAllowed("APPLICATION")
    @Operation(
            summary = "Disable user",
            description = "Disables the user, revokes refresh tokens and blocks new logins. Existing access tokens are rejected on the next request."
    )
    @APIResponse(
            responseCode = "200",
            description = "User disabled",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Disabled user",
                            value = UserExamples.USER_CREATED)
            )
    )
    public Response disable(@PathParam(RequestParams.USER_ID) String userId) {
        return Response.ok(new UserResponseDto(this.userController.disableUser(userId))).build();
    }

    @PATCH
    @Path("/{userId}/enable")
    @RolesAllowed("APPLICATION")
    @Operation(
            summary = "Enable user",
            description = "Re-enables a previously disabled user."
    )
    @APIResponse(
            responseCode = "200",
            description = "User enabled",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Enabled user",
                            value = UserExamples.USER_CREATED)
            )
    )
    public Response enable(@PathParam(RequestParams.USER_ID) String userId) {
        return Response.ok(new UserResponseDto(this.userController.enableUser(userId))).build();
    }

    @POST
    @Path("/create")
    @RolesAllowed("APPLICATION")
    @Operation(
            summary = "Create a new user",
            description = "Creates a new user with the provided details and sends a verification email when an address is present."
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
    public Response create(@NotNull @Valid CreateUserRequestDto request) {
        User user = this.userController.createUser(request);

        return Response.status(Response.Status.CREATED)
                .entity(new UserResponseDto(user))
                .build();
    }

    @POST
    @Path("/{userId}/email/verification/send")
    @RolesAllowed({"APPLICATION", "USER"})
    @Operation(
            summary = "Send user email verification",
            description = "Generates a verification token and sends an email to the user email."
    )
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Send email verification",
                            value = UserExamples.SEND_EMAIL_VERIFICATION)
            )
    )
    @APIResponse(responseCode = "204", description = "Verification email sent successfully")
    public Response sendEmailVerification(@PathParam(RequestParams.USER_ID) String userId,
                                          @NotNull @Valid SendEmailVerificationRequestDto request) {
        this.userController.sendEmailVerification(userId, request.email());
        return Response.noContent().build();
    }

    @GET
    @Path("/email/verify")
    @PermitAll
    @Operation(
            summary = "Verify user email",
            description = "Confirms the user email with a token received by email."
    )
    @APIResponse(responseCode = "204", description = "Email verified successfully")
    public Response verifyEmail(@QueryParam(RequestParams.TOKEN) @NotBlank String token) {
        this.userController.verifyEmail(token);
        return Response.noContent().build();
    }

    @POST
    @Path("/password/forgot")
    @PermitAll
    @Operation(
            summary = "Request password reset",
            description = "Sends a password reset email to the given user email in the specified application."
    )
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Forgot password",
                            value = UserExamples.FORGOT_PASSWORD)
            )
    )
    @APIResponse(responseCode = "204", description = "Password reset email sent if the account exists")
    public Response forgotPassword(@NotNull @Valid ForgotPasswordRequestDto request) {
        this.userController.forgotPassword(request.email(), request.appId());
        return Response.noContent().build();
    }

    @POST
    @Path("/password/reset")
    @PermitAll
    @Operation(
            summary = "Reset user password",
            description = "Resets the user password using a valid reset token."
    )
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Reset password",
                            value = UserExamples.RESET_PASSWORD)
            )
    )
    @APIResponse(responseCode = "204", description = "Password reset successfully")
    public Response resetPassword(@NotNull @Valid ResetPasswordRequestDto request) {
        this.userController.resetPassword(request.token(), request.newPassword());
        return Response.noContent().build();
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
