package com.mtsolutions.application.resource.rest;

import com.mtsolutions.application.common.RequestParams;
import com.mtsolutions.application.resource.rest.examples.OwnerExamples;
import com.mtsolutions.domain.constant.ImageType;
import com.mtsolutions.domain.controller.OwnerController;
import com.mtsolutions.domain.dto.request.CreateAddressRequestDto;
import com.mtsolutions.domain.dto.request.CreateOwnerRequestDto;
import com.mtsolutions.domain.dto.request.OwnerForgotPasswordRequestDto;
import com.mtsolutions.domain.dto.request.ResetPasswordRequestDto;
import com.mtsolutions.domain.dto.request.SendEmailVerificationRequestDto;
import com.mtsolutions.domain.dto.request.UpdateOwnerRequestDto;
import com.mtsolutions.domain.dto.request.UploadOwnerImageRequestDto;
import com.mtsolutions.domain.dto.response.OwnerResponseDto;
import com.mtsolutions.domain.entity.Owner;
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
@Path("/api/v1/owner")
@Tag(name = "Owner", description = "Owner API")
public class OwnerResource {

    private final OwnerController ownerController;

    public OwnerResource(OwnerController ownerController) {
        this.ownerController = ownerController;
    }

    @GET
    @Path("/me")
    @RolesAllowed({"OWNER", "OWNER_WRITER", "OWNER_VIEWER"})
    @Operation(
            summary = "Get the authenticated owner",
            description = "Returns the profile of the owner identified by the access token."
    )
    @APIResponse(
            responseCode = "200",
            description = "Current owner",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Current owner",
                            value = OwnerExamples.OWNER_CREATED)
            )
    )
    public Response me() {
        return Response.ok(new OwnerResponseDto(this.ownerController.findCurrentOwner())).build();
    }

    @PATCH
    @Path("/me")
    @RolesAllowed({"OWNER", "OWNER_WRITER", "OWNER_VIEWER"})
    @Operation(summary = "Update the authenticated owner", description = "Updates name, phone and document of the current owner.")
    @APIResponse(responseCode = "200", description = "Owner updated")
    public Response updateMe(@NotNull @Valid UpdateOwnerRequestDto request) {
        return Response.ok(new OwnerResponseDto(this.ownerController.updateCurrentOwner(request))).build();
    }

    @PATCH
    @Path("/me/address")
    @RolesAllowed({"OWNER", "OWNER_WRITER", "OWNER_VIEWER"})
    @Operation(summary = "Attach address to the authenticated owner", description = "Appends a complete address. Use GET /addresses/{country}/{zipCode} to look up street/city first.")
    @APIResponse(responseCode = "200", description = "Address attached")
    public Response attachAddress(@NotNull @Valid CreateAddressRequestDto request) {
        return Response.ok(new OwnerResponseDto(this.ownerController.attachAddressToCurrentOwner(request))).build();
    }

    @DELETE
    @Path("/me/address/{addressIndex}")
    @RolesAllowed({"OWNER", "OWNER_WRITER", "OWNER_VIEWER"})
    @Operation(summary = "Remove address from the authenticated owner")
    @APIResponse(responseCode = "204", description = "Address removed")
    public Response removeAddress(@PathParam(RequestParams.ADDRESS_INDEX) Integer addressIndex) {
        this.ownerController.removeAddressFromCurrentOwner(addressIndex);
        return Response.noContent().build();
    }

    @POST
    @Path("/me/images/{imageType}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({"OWNER", "OWNER_WRITER", "OWNER_VIEWER"})
    @Operation(summary = "Upload owner image", description = "Uploads a profile picture to Cloudinary.")
    @APIResponse(responseCode = "200", description = "Image uploaded")
    public Response uploadImage(@PathParam(RequestParams.IMAGE_TYPE) ImageType imageType,
                                @RestForm("image") FileUpload image) {
        Owner owner = this.ownerController.uploadCurrentOwnerImage(new UploadOwnerImageRequestDto(
                imageType,
                image.uploadedFile(),
                image.fileName(),
                image.size(),
                image.contentType()
        ));
        return Response.ok(new OwnerResponseDto(owner)).build();
    }

    @DELETE
    @Path("/me/images/{imageType}")
    @RolesAllowed({"OWNER", "OWNER_WRITER", "OWNER_VIEWER"})
    @Operation(summary = "Remove owner image")
    @APIResponse(responseCode = "204", description = "Image removed")
    public Response removeImage(@PathParam(RequestParams.IMAGE_TYPE) ImageType imageType) {
        this.ownerController.removeCurrentOwnerImage(imageType);
        return Response.noContent().build();
    }

    @POST
    @Path("/create")
    @PermitAll
    @Operation(
            summary = "Create a new owner",
            description = "Public signup. The first owner has email already verified. Writer and viewer roles belong to each application, not to this account. Address is required. A verification email is sent when the email is not already verified."
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
    public Response create(@NotNull @Valid CreateOwnerRequestDto request) {
        Owner owner = this.ownerController.createOwner(request);

        return Response.status(Response.Status.CREATED)
                .entity(new OwnerResponseDto(owner))
                .build();
    }

    @POST
    @Path("/{ownerId}/email/verification/send")
    @RolesAllowed({"OWNER", "OWNER_WRITER", "OWNER_VIEWER"})
    @Operation(
            summary = "Send owner email verification",
            description = "Generates a verification token and sends an email to the owner email."
    )
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Send owner email verification",
                            value = OwnerExamples.SEND_EMAIL_VERIFICATION)
            )
    )
    @APIResponse(responseCode = "204", description = "Verification email sent successfully")
    public Response sendEmailVerification(@PathParam(RequestParams.OWNER_ID) String ownerId,
                                          @NotNull @Valid SendEmailVerificationRequestDto request) {
        this.ownerController.sendEmailVerification(ownerId, request.email());
        return Response.noContent().build();
    }

    @GET
    @Path("/email/verify")
    @PermitAll
    @Operation(
            summary = "Verify owner email",
            description = "Confirms the owner email with a token received by email."
    )
    @APIResponse(responseCode = "204", description = "Email verified successfully")
    public Response verifyEmail(@QueryParam(RequestParams.TOKEN) @NotBlank String token) {
        this.ownerController.verifyEmail(token);
        return Response.noContent().build();
    }

    @POST
    @Path("/password/forgot")
    @PermitAll
    @Operation(
            summary = "Request owner password reset",
            description = "Sends a password reset email to the owner email."
    )
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Owner forgot password",
                            value = OwnerExamples.FORGOT_PASSWORD)
            )
    )
    @APIResponse(responseCode = "204", description = "Password reset email sent if the account exists")
    public Response forgotPassword(@NotNull @Valid OwnerForgotPasswordRequestDto request) {
        this.ownerController.forgotPassword(request.email());
        return Response.noContent().build();
    }

    @POST
    @Path("/password/reset")
    @PermitAll
    @Operation(
            summary = "Reset owner password",
            description = "Resets the owner password using a valid reset token."
    )
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = @ExampleObject(
                            name = "Owner reset password",
                            value = OwnerExamples.RESET_PASSWORD)
            )
    )
    @APIResponse(responseCode = "204", description = "Password reset successfully")
    public Response resetPassword(@NotNull @Valid ResetPasswordRequestDto request) {
        this.ownerController.resetPassword(request.token(), request.newPassword());
        return Response.noContent().build();
    }
}
