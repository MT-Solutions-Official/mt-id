package com.mtsolutions.application.model;

import com.mtsolutions.domain.entity.ClientApplication;
import com.mtsolutions.domain.model.EmailSettings;

public record EmailBranding(
        String appName,
        String logoUrl,
        String fromEmail,
        String fromName,
        String replyTo,
        String supportEmail,
        String supportUrl
) {

    public static EmailBranding of(String appName,
                                   String logoUrl,
                                   String fromEmail,
                                   String fromName,
                                   String replyTo,
                                   String supportEmail,
                                   String supportUrl) {
        return new EmailBranding(appName, logoUrl, fromEmail, fromName, replyTo, supportEmail, supportUrl);
    }

    public static EmailBranding fromClientApplication(ClientApplication clientApplication) {
        EmailSettings settings = clientApplication.getEmailSettings();
        return new EmailBranding(
                clientApplication.getName(),
                clientApplication.getLogoUrl(),
                settings != null ? settings.getFromEmail() : null,
                settings != null ? settings.getFromName() : null,
                settings != null ? settings.getReplyTo() : null,
                settings != null ? settings.getSupportEmail() : null,
                settings != null ? settings.getSupportUrl() : null
        );
    }

    public String resolveFromName() {
        return this.fromName != null && !this.fromName.isBlank() ? this.fromName : this.appName;
    }
}
