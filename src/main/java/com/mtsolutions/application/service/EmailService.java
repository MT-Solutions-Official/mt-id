package com.mtsolutions.application.service;

import com.mtsolutions.application.model.EmailBranding;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Slf4j
public class EmailService {

    @ConfigProperty(name = "quarkus.mailer.from")
    String defaultFrom;

    private final Mailer mailer;
    private final Template verificationEmailTemplate;
    private final Template passwordResetTemplate;
    private final Template passwordChangedTemplate;

    public EmailService(Mailer mailer,
                        @Location("emails/verification-email.html") Template verificationEmailTemplate,
                        @Location("emails/password-reset.html") Template passwordResetTemplate,
                        @Location("emails/password-changed.html") Template passwordChangedTemplate) {
        this.mailer = mailer;
        this.verificationEmailTemplate = verificationEmailTemplate;
        this.passwordResetTemplate = passwordResetTemplate;
        this.passwordChangedTemplate = passwordChangedTemplate;
    }

    public void sendVerificationEmail(EmailBranding branding, String recipientEmail, String recipientName, String verificationUrl) {
        this.sendTemplatedEmail(
                this.verificationEmailTemplate,
                branding,
                recipientEmail,
                new EmailTemplatePayload(
                        recipientName,
                        "Confirme seu e-mail",
                        verificationUrl,
                        "Confirmar e-mail",
                        "Clique no botão abaixo para confirmar seu endereço de e-mail e ativar sua conta.",
                        "Se você não criou uma conta, ignore este e-mail.",
                        null
                )
        );
    }

    public void sendPasswordResetEmail(EmailBranding branding, String recipientEmail, String recipientName, String resetUrl) {
        this.sendTemplatedEmail(
                this.passwordResetTemplate,
                branding,
                recipientEmail,
                new EmailTemplatePayload(
                        recipientName,
                        "Redefinição de senha",
                        resetUrl,
                        "Redefinir senha",
                        "Recebemos uma solicitação para redefinir sua senha. Clique no botão abaixo para criar uma nova.",
                        "Se você não solicitou a redefinição, ignore este e-mail.",
                        null
                )
        );
    }

    public void sendPasswordChangedEmail(EmailBranding branding, String recipientEmail, String recipientName, String changedAt, String accountUrl) {
        this.sendTemplatedEmail(
                this.passwordChangedTemplate,
                branding,
                recipientEmail,
                new EmailTemplatePayload(
                        recipientName,
                        "Sua senha foi alterada",
                        accountUrl,
                        "Acessar conta",
                        "Registramos a alteração da senha da sua conta.",
                        "Se não foi você, entre em contato imediatamente com o suporte.",
                        changedAt
                )
        );
    }

    private void sendTemplatedEmail(Template template,
                                    EmailBranding branding,
                                    String recipientEmail,
                                    EmailTemplatePayload payload) {
        String htmlBody = template
                .data("appName", branding.appName())
                .data("logoUrl", branding.logoUrl())
                .data("recipientName", payload.recipientName())
                .data("primaryMessage", payload.primaryMessage())
                .data("secondaryMessage", payload.secondaryMessage())
                .data("actionUrl", payload.actionUrl())
                .data("actionText", payload.actionText())
                .data("supportEmail", branding.supportEmail())
                .data("supportUrl", branding.supportUrl())
                .data("changedAt", payload.changedAt())
                .render();

        Mail mail = Mail.withHtml(recipientEmail, payload.subject(), htmlBody)
                .setFrom(resolveFrom(branding));

        if (branding.replyTo() != null && !branding.replyTo().isBlank()) {
            mail.addReplyTo(branding.replyTo());
        }

        log.info("Sending email '{}' to {}", payload.subject(), recipientEmail);
        this.mailer.send(mail);
    }

    private String resolveFrom(EmailBranding branding) {
        if (branding.fromEmail() != null && !branding.fromEmail().isBlank()) {
            return branding.resolveFromName() + " <" + branding.fromEmail() + ">";
        }

        return defaultFrom;
    }

    private record EmailTemplatePayload(
            String recipientName,
            String subject,
            String actionUrl,
            String actionText,
            String primaryMessage,
            String secondaryMessage,
            String changedAt
    ) {
    }
}
