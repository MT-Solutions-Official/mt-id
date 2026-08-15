package com.mtsolutions.application.service;

import com.mtsolutions.domain.entity.ClientApplication;
import com.mtsolutions.domain.model.EmailSettings;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@ApplicationScoped
public class EmailLinkService {

    @ConfigProperty(name = "app.mt.id.base-url")
    String apiBaseUrl;

    @ConfigProperty(name = "app.mt.id.frontend.base-url")
    String frontendBaseUrl;

    public String userVerificationUrl(ClientApplication clientApplication, String token) {
        String configured = settings(clientApplication) != null ? settings(clientApplication).getVerificationRedirectUrl() : null;
        return appendToken(firstValidRedirect(clientApplication, configured, userLanding("/verify")), token);
    }

    public String userPasswordResetUrl(ClientApplication clientApplication, String token) {
        String configured = settings(clientApplication) != null ? settings(clientApplication).getPasswordResetRedirectUrl() : null;
        return appendToken(firstValidRedirect(clientApplication, configured, userLanding("/reset-password")), token);
    }

    public String userAccountUrl(ClientApplication clientApplication) {
        EmailSettings emailSettings = settings(clientApplication);
        String loginUrl = emailSettings != null ? emailSettings.getLoginUrl() : null;
        if (isAllowedRedirect(clientApplication, loginUrl)) {
            return trimTrailingSlash(loginUrl);
        }
        String origin = firstAllowedOrigin(clientApplication);
        if (origin != null) {
            return origin;
        }
        return trimTrailingSlash(this.frontendBaseUrl);
    }

    public String ownerVerificationUrl(String token) {
        return appendToken(ownerLanding("/verify"), token);
    }

    public String ownerPasswordResetUrl(String token) {
        return appendToken(ownerLanding("/reset-password"), token);
    }

    public String ownerAccountUrl() {
        return trimTrailingSlash(this.frontendBaseUrl);
    }

    public String appendToken(String url, String token) {
        if (url == null || url.isBlank()) {
            return url;
        }
        String separator = url.contains("?") ? "&" : "?";
        return url + separator + "token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    private String userLanding(String path) {
        return trimTrailingSlash(this.apiBaseUrl) + "/api/v1/email/users" + path;
    }

    private String ownerLanding(String path) {
        return trimTrailingSlash(this.apiBaseUrl) + "/api/v1/email/owners" + path;
    }

    private String firstValidRedirect(ClientApplication clientApplication, String configuredUrl, String fallback) {
        if (isAllowedRedirect(clientApplication, configuredUrl)) {
            return configuredUrl.trim();
        }
        return fallback;
    }

    private boolean isAllowedRedirect(ClientApplication clientApplication, String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        URI uri;
        try {
            uri = URI.create(url.trim());
        } catch (IllegalArgumentException e) {
            return false;
        }

        if (uri.getScheme() == null || uri.getHost() == null) {
            return false;
        }
        if (!"https".equalsIgnoreCase(uri.getScheme()) && !isLocalHttp(uri)) {
            return false;
        }

        List<String> allowedOrigins = clientApplication.getAllowedOrigins();
        if (allowedOrigins == null || allowedOrigins.isEmpty()) {
            return true;
        }

        String origin = uri.getScheme() + "://" + uri.getAuthority();
        return allowedOrigins.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(this::trimTrailingSlash)
                .anyMatch(allowed -> allowed.equalsIgnoreCase(origin));
    }

    private boolean isLocalHttp(URI uri) {
        if (!"http".equalsIgnoreCase(uri.getScheme())) {
            return false;
        }
        String host = uri.getHost();
        return "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host);
    }

    private String firstAllowedOrigin(ClientApplication clientApplication) {
        List<String> allowedOrigins = clientApplication.getAllowedOrigins();
        if (allowedOrigins == null) {
            return null;
        }
        return allowedOrigins.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(this::trimTrailingSlash)
                .findFirst()
                .orElse(null);
    }

    private EmailSettings settings(ClientApplication clientApplication) {
        return clientApplication != null ? clientApplication.getEmailSettings() : null;
    }

    private String trimTrailingSlash(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
