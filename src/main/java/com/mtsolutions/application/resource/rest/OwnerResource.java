package com.mtsolutions.application.resource.rest;

import com.mtsolutions.application.common.RequestParams;
import com.mtsolutions.application.resource.rest.examples.OwnerExamples;
import com.mtsolutions.domain.controller.OwnerController;
import com.mtsolutions.domain.dto.request.CreateOwnerRequestDto;
import com.mtsolutions.domain.dto.request.ForgotPasswordRequestDto;
import com.mtsolutions.domain.dto.request.ResetPasswordRequestDto;
import com.mtsolutions.domain.dto.request.SendEmailVerificationRequestDto;
import com.mtsolutions.domain.entity.Owner;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
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
    public Response create(@NotNull @Valid CreateOwnerRequestDto request) {
        Owner owner = this.ownerController.createOwner(request);

        return Response.status(Response.Status.CREATED)
                .entity(owner)
                .build();
    }

    @POST
    @Path("/{ownerId}/email/verification/send")
    @RolesAllowed({"OWNER_WRITER"})
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
    public Response forgotPassword(@NotNull @Valid ForgotPasswordRequestDto request) {
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
