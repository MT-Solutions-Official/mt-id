package com.mtsolutions.application.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtsolutions.application.cache.AllowedOriginCache;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;

@ApplicationScoped
public class TenantCorsFilter {

    private static final Set<String> ALLOWED_METHODS = Set.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    private static final String ALLOWED_HEADERS = "Content-Type, Authorization, Accept, apiKey, apiSecret, appId";

    @ConfigProperty(name = "app.mt.id.cors.platform-origin", defaultValue = "http://localhost:3000")
    String platformOrigin;

    private final AllowedOriginCache allowedOriginCache;
    private final ObjectMapper objectMapper;

    public TenantCorsFilter(AllowedOriginCache allowedOriginCache, ObjectMapper objectMapper) {
        this.allowedOriginCache = allowedOriginCache;
        this.objectMapper = objectMapper;
    }

    void register(@Observes Router router) {
        router.route().order(Integer.MIN_VALUE).handler(context -> {
            String origin = context.request().getHeader("Origin");
            if (this.isAllowedOrigin(context.request(), origin)) {
                context.response()
                        .putHeader("Access-Control-Allow-Origin", origin)
                        .putHeader("Access-Control-Allow-Credentials", "true")
                        .putHeader("Access-Control-Allow-Methods", String.join(", ", ALLOWED_METHODS))
                        .putHeader("Access-Control-Allow-Headers", ALLOWED_HEADERS)
                        .putHeader("Access-Control-Max-Age", "3600")
                        .putHeader("Vary", "Origin");
            }

            if ("OPTIONS".equalsIgnoreCase(context.request().method().name())) {
                context.response().setStatusCode(204).end();
                return;
            }

            context.next();
        });
    }

    private boolean isAllowedOrigin(HttpServerRequest request, String origin) {
        String normalizedOrigin = AllowedOriginCache.normalizeOrigin(origin);
        if (normalizedOrigin == null) {
            return false;
        }
        if (normalizedOrigin.equals(AllowedOriginCache.normalizeOrigin(this.platformOrigin))) {
            return true;
        }

        String appId = this.resolveAppId(request);
        return this.allowedOriginCache.isOriginAllowed(appId, normalizedOrigin);
    }

    private String resolveAppId(HttpServerRequest request) {
        String header = trimToNull(request.getHeader("appId"));
        if (header != null) {
            return header;
        }
        String query = trimToNull(request.getParam("appId"));
        if (query != null) {
            return query;
        }
        return this.peekAppIdFromAuthorization(request.getHeader("Authorization"));
    }

    private String peekAppIdFromAuthorization(String authorization) {
        if (authorization == null || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        String token = authorization.substring(7).trim();
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            return null;
        }
        try {
            byte[] payload = Base64.getUrlDecoder().decode(padBase64(parts[1]));
            JsonNode node = this.objectMapper.readTree(new String(payload, StandardCharsets.UTF_8));
            JsonNode appId = node.get("app_id");
            return appId != null && !appId.isNull() ? trimToNull(appId.asText()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String padBase64(String value) {
        int remainder = value.length() % 4;
        if (remainder == 0) {
            return value;
        }
        return value + "=".repeat(4 - remainder);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
