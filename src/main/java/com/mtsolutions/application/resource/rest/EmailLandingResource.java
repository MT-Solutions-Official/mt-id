package com.mtsolutions.application.resource.rest;

import com.mtsolutions.application.exception.MtIdException;
import com.mtsolutions.application.exception.PwnedPasswordException;
import com.mtsolutions.application.exception.WeakPasswordException;
import com.mtsolutions.domain.controller.OwnerController;
import com.mtsolutions.domain.controller.UserController;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@RequestScoped
@Path("/api/v1/email")
@Tag(name = "Email Pages", description = "HTML pages opened from account emails")
public class EmailLandingResource {

    private final UserController userController;
    private final OwnerController ownerController;
    private final Template emailStatusTemplate;
    private final Template passwordResetFormTemplate;

    public EmailLandingResource(UserController userController,
                                OwnerController ownerController,
                                @Location("pages/email-status.html") Template emailStatusTemplate,
                                @Location("pages/password-reset-form.html") Template passwordResetFormTemplate) {
        this.userController = userController;
        this.ownerController = ownerController;
        this.emailStatusTemplate = emailStatusTemplate;
        this.passwordResetFormTemplate = passwordResetFormTemplate;
    }

    @GET
    @Path("/users/verify")
    @PermitAll
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Verify user email from the message link")
    public Response verifyUserEmail(@QueryParam("token") String token) {
        try {
            this.userController.verifyEmail(token);
            return html(200, status("Verificação", "#4F46E5", "E-mail confirmado",
                    "Sua conta está pronta. Você já pode voltar ao aplicativo e entrar.", null, null));
        } catch (MtIdException e) {
            return html(400, status("Verificação", "#b91c1c", "Link inválido ou expirado",
                    "Solicite um novo e-mail de verificação no aplicativo.", null, null));
        }
    }

    @GET
    @Path("/owners/verify")
    @PermitAll
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Verify owner email from the message link")
    public Response verifyOwnerEmail(@QueryParam("token") String token) {
        try {
            this.ownerController.verifyEmail(token);
            return html(200, status("Verificação", "#4F46E5", "E-mail confirmado",
                    "Seu acesso à plataforma MT ID está verificado.", null, null));
        } catch (MtIdException e) {
            return html(400, status("Verificação", "#b91c1c", "Link inválido ou expirado",
                    "Solicite um novo e-mail de verificação.", null, null));
        }
    }

    @GET
    @Path("/users/reset-password")
    @PermitAll
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Show user password reset form")
    public Response userResetForm(@QueryParam("token") String token) {
        return resetForm("/api/v1/email/users/reset-password", token, null);
    }

    @POST
    @Path("/users/reset-password")
    @PermitAll
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Reset user password from the email form")
    public Response resetUserPassword(@FormParam("token") String token,
                                      @FormParam("newPassword") String newPassword,
                                      @FormParam("confirmPassword") String confirmPassword) {
        String error = validatePasswords(newPassword, confirmPassword);
        if (error != null) {
            return resetForm("/api/v1/email/users/reset-password", token, error);
        }
        try {
            this.userController.resetPassword(token, newPassword);
            return html(200, status("Segurança", "#047857", "Senha atualizada",
                    "Sua senha foi redefinida. Volte ao aplicativo e entre com a nova senha.", null, null));
        } catch (WeakPasswordException e) {
            return resetForm("/api/v1/email/users/reset-password", token,
                    "A senha deve ter pelo menos 8 caracteres, com letra maiúscula, minúscula, número e caractere especial.");
        } catch (PwnedPasswordException e) {
            return resetForm("/api/v1/email/users/reset-password", token,
                    "Essa senha apareceu em um vazamento e não pode ser usada.");
        } catch (MtIdException e) {
            return html(400, status("Segurança", "#b91c1c", "Link inválido ou expirado",
                    "Solicite uma nova redefinição de senha.", null, null));
        }
    }

    @GET
    @Path("/owners/reset-password")
    @PermitAll
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Show owner password reset form")
    public Response ownerResetForm(@QueryParam("token") String token) {
        return resetForm("/api/v1/email/owners/reset-password", token, null);
    }

    @POST
    @Path("/owners/reset-password")
    @PermitAll
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Reset owner password from the email form")
    public Response resetOwnerPassword(@FormParam("token") String token,
                                       @FormParam("newPassword") String newPassword,
                                       @FormParam("confirmPassword") String confirmPassword) {
        String error = validatePasswords(newPassword, confirmPassword);
        if (error != null) {
            return resetForm("/api/v1/email/owners/reset-password", token, error);
        }
        try {
            this.ownerController.resetPassword(token, newPassword);
            return html(200, status("Segurança", "#047857", "Senha atualizada",
                    "Sua senha da plataforma MT ID foi redefinida.", null, null));
        } catch (WeakPasswordException e) {
            return resetForm("/api/v1/email/owners/reset-password", token,
                    "A senha deve ter pelo menos 8 caracteres, com letra maiúscula, minúscula, número e caractere especial.");
        } catch (PwnedPasswordException e) {
            return resetForm("/api/v1/email/owners/reset-password", token,
                    "Essa senha apareceu em um vazamento e não pode ser usada.");
        } catch (MtIdException e) {
            return html(400, status("Segurança", "#b91c1c", "Link inválido ou expirado",
                    "Solicite uma nova redefinição de senha.", null, null));
        }
    }

    private String validatePasswords(String newPassword, String confirmPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            return "Informe a nova senha.";
        }
        if (!newPassword.equals(confirmPassword)) {
            return "As senhas não coincidem.";
        }
        return null;
    }

    private Response resetForm(String actionUrl, String token, String error) {
        String html = this.passwordResetFormTemplate
                .data("actionUrl", actionUrl)
                .data("token", token != null ? token : "")
                .data("error", error)
                .render();
        return html(error == null ? 200 : 400, html);
    }

    private String status(String badgeText, String accentColor, String title, String message, String actionUrl, String actionText) {
        return this.emailStatusTemplate
                .data("badgeText", badgeText)
                .data("accentColor", accentColor)
                .data("title", title)
                .data("message", message)
                .data("actionUrl", actionUrl)
                .data("actionText", actionText)
                .data("detail", null)
                .render();
    }

    private Response html(int status, String body) {
        return Response.status(status)
                .type(MediaType.TEXT_HTML_TYPE.withCharset("utf-8"))
                .entity(body)
                .build();
    }
}
