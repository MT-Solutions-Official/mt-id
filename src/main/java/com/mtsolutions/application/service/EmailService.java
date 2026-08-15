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
    private final Template emailTemplate;

    public EmailService(Mailer mailer,
                        @Location("emails/base.html") Template emailTemplate) {
        this.mailer = mailer;
        this.emailTemplate = emailTemplate;
    }

    public void sendVerificationEmail(EmailBranding branding, String recipientEmail, String recipientName, String verificationUrl) {
        this.sendTemplatedEmail(
                branding,
                recipientEmail,
                new EmailTemplatePayload(
                        recipientName,
                        "Confirme seu e-mail",
                        "Confirme seu e-mail",
                        verificationUrl,
                        "Confirmar e-mail",
                        "Para ativar sua conta, confirme que este endereço de e-mail é seu.",
                        "Este link expira em breve. Se você não criou uma conta, ignore este e-mail.",
                        null,
                        "#4F46E5",
                        "Verificação"
                )
        );
    }

    public void sendPasswordResetEmail(EmailBranding branding, String recipientEmail, String recipientName, String resetUrl) {
        this.sendTemplatedEmail(
                branding,
                recipientEmail,
                new EmailTemplatePayload(
                        recipientName,
                        "Redefinição de senha",
                        "Redefinir sua senha",
                        resetUrl,
                        "Redefinir senha",
                        "Recebemos um pedido para redefinir a senha da sua conta.",
                        "O link é válido por pouco tempo. Se você não fez esta solicitação, nenhuma ação é necessária.",
                        null,
                        "#C2410C",
                        "Segurança"
                )
        );
    }

    public void sendPasswordChangedEmail(EmailBranding branding, String recipientEmail, String recipientName, String changedAt, String accountUrl) {
        this.sendTemplatedEmail(
                branding,
                recipientEmail,
                new EmailTemplatePayload(
                        recipientName,
                        "Sua senha foi alterada",
                        "Senha atualizada",
                        accountUrl,
                        "Acessar conta",
                        "A senha da sua conta foi alterada com sucesso.",
                        "Se não foi você, redefina a senha imediatamente e fale com o suporte.",
                        changedAt,
                        "#047857",
                        "Alerta"
                )
        );
    }

    private void sendTemplatedEmail(EmailBranding branding,
                                    String recipientEmail,
                                    EmailTemplatePayload payload) {
        String htmlBody = this.emailTemplate
                .data("appName", branding.appName())
                .data("logoUrl", branding.logoUrl())
                .data("accentColor", payload.accentColor())
                .data("badgeText", payload.badgeText())
                .data("title", payload.title())
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
                .setText(buildTextBody(branding, payload))
                .setFrom(resolveFrom(branding));

        String replyTo = resolveReplyTo(branding);
        if (replyTo != null) {
            mail.addReplyTo(replyTo);
        }

        log.info("Sending email '{}' to {}", payload.subject(), recipientEmail);
        this.mailer.send(mail);
    }

    private String buildTextBody(EmailBranding branding, EmailTemplatePayload payload) {
        StringBuilder text = new StringBuilder();
        text.append(branding.appName()).append("\n\n");
        text.append("Olá ").append(payload.recipientName()).append(",\n\n");
        text.append(payload.primaryMessage()).append("\n\n");
        text.append(payload.secondaryMessage()).append("\n\n");
        if (payload.actionUrl() != null && !payload.actionUrl().isBlank()) {
            text.append(payload.actionText()).append(": ").append(payload.actionUrl()).append("\n\n");
        }
        if (payload.changedAt() != null) {
            text.append("Alteração registrada em: ").append(payload.changedAt()).append("\n\n");
        }
        if (branding.supportEmail() != null && !branding.supportEmail().isBlank()) {
            text.append("Suporte: ").append(branding.supportEmail()).append("\n");
        }
        return text.toString();
    }

    private String resolveFrom(EmailBranding branding) {
        String address = extractEmailAddress(this.defaultFrom);
        String fromName = branding.resolveFromName();
        if (fromName != null && !fromName.isBlank()) {
            return fromName + " <" + address + ">";
        }
        return address;
    }

    private String resolveReplyTo(EmailBranding branding) {
        if (branding.replyTo() != null && !branding.replyTo().isBlank()) {
            return branding.replyTo().trim();
        }
        if (branding.supportEmail() != null && !branding.supportEmail().isBlank()) {
            return branding.supportEmail().trim();
        }
        return null;
    }

    private String extractEmailAddress(String from) {
        if (from == null || from.isBlank()) {
            return from;
        }
        String value = from.trim();
        int start = value.indexOf('<');
        int end = value.indexOf('>');
        if (start >= 0 && end > start) {
            return value.substring(start + 1, end).trim();
        }
        return value;
    }

    private record EmailTemplatePayload(
            String recipientName,
            String subject,
            String title,
            String actionUrl,
            String actionText,
            String primaryMessage,
            String secondaryMessage,
            String changedAt,
            String accentColor,
            String badgeText
    ) {
    }
}
