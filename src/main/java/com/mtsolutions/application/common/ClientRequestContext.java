package com.mtsolutions.application.common;

import io.vertx.core.http.HttpServerRequest;
import jakarta.enterprise.context.RequestScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@RequestScoped
public class ClientRequestContext {

    @ConfigProperty(name = "app.mt.id.http.trust-forwarded-headers", defaultValue = "true")
    boolean trustForwardedHeaders;

    private final HttpServerRequest request;

    public ClientRequestContext(HttpServerRequest request) {
        this.request = request;
    }

    public String clientIp() {
        if (this.trustForwardedHeaders) {
            String forwarded = header("X-Forwarded-For");
            if (forwarded != null) {
                int comma = forwarded.indexOf(',');
                return comma >= 0 ? forwarded.substring(0, comma).trim() : forwarded;
            }
            String realIp = header("X-Real-IP");
            if (realIp != null) {
                return realIp;
            }
        }

        if (this.request != null && this.request.remoteAddress() != null) {
            String host = this.request.remoteAddress().host();
            if (host != null && !host.isBlank()) {
                return host.trim();
            }
        }
        return "unknown";
    }

    public String appIdHeader() {
        return header("appId");
    }

    public String origin() {
        return header("Origin");
    }

    private String header(String name) {
        if (this.request == null) {
            return null;
        }
        String value = this.request.getHeader(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
