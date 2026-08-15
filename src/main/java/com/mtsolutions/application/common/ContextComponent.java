package com.mtsolutions.application.common;

import com.mtsolutions.application.exception.ApplicationForbiddenException;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

@ApplicationScoped
@Slf4j
public class ContextComponent {

    private final JsonWebToken jwt;
    private final SecurityIdentity identity;

    public ContextComponent(JsonWebToken jwt, SecurityIdentity identity) {
        this.jwt = jwt;
        this.identity = identity;
    }

    public String getDocumentId() {
        return jwt.getSubject();
    }

    public String getUserId() { return jwt.getClaim("userId"); }

    public String getEmail() {
        return jwt.getClaim("upn");
    }

    public String getName() {
        return jwt.getClaim("name");
    }

    public boolean hasRole(String role) {
        return role != null && this.identity.getRoles().contains(role);
    }

    public String getRole() {
        if (this.hasRole("APPLICATION")) {
            return "APPLICATION";
        }
        if (this.hasRole("REFRESH_TOKEN")) {
            return "REFRESH_TOKEN";
        }
        if (this.hasRole("USER")) {
            return "USER";
        }
        return identity.getRoles().stream().findFirst().orElse(null);
    }


    public String getAuthenticatedAppId() {
        String appId = this.jwt.getClaim("app_id");
        if (appId == null || appId.isBlank()) {
            throw new ApplicationForbiddenException();
        }

        return appId;
    }

    public String getAuthenticatedAppIdOrNull() {
        return this.jwt.getClaim("app_id");
    }

    public String getAuthenticatedUserIdOrNull() {
        return this.jwt.getClaim("userId");
    }

    public String getAuthenticatedTokenTypeOrNull() {
        return this.jwt.getClaim("token_type");
    }

    public String getAuthenticatedTokenIdOrNull() {
        return this.jwt.getClaim("jti");
    }

    public String getAuthenticatedOwnerId() {
        String ownerId = this.jwt.getClaim("ownerId");
        if (ownerId == null || ownerId.isBlank()) {
            throw new ApplicationForbiddenException();
        }

        return ownerId;
    }

    public void validateAppOrSelfAccess(String resourceAppId, String resourceUserId) {
        String authenticatedAppId = this.getAuthenticatedAppIdOrNull();
        if (this.hasRole("APPLICATION")
                && authenticatedAppId != null
                && authenticatedAppId.equals(resourceAppId)) {
            return;
        }

        String authenticatedUserId = this.getAuthenticatedUserIdOrNull();
        if (this.hasRole("USER")
                && authenticatedUserId != null
                && authenticatedUserId.equals(resourceUserId)
                && authenticatedAppId != null
                && authenticatedAppId.equals(resourceAppId)) {
            return;
        }

        throw new ApplicationForbiddenException();
    }

}
