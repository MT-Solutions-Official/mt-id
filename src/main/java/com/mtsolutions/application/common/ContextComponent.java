package com.mtsolutions.application.common;

import com.mtsolutions.application.utils.DateUtils;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.UUID;

@ApplicationScoped
@Slf4j
public class ContextComponent {

    private final JsonWebToken jwt;
    private final SecurityIdentity identity;
    private final DateUtils dateUtils;

    public ContextComponent(JsonWebToken jwt, SecurityIdentity identity, DateUtils dateUtils) {
        this.jwt = jwt;
        this.identity = identity;
        this.dateUtils = dateUtils;
    }

    public String getDocumentId() {
        return jwt.getSubject();
    }

    public UUID getUserId() {return jwt.getClaim("userId");}

    public String getEmail() {
        return jwt.getClaim("upn");
    }

    public String getName() {
        return jwt.getClaim("name");
    }

    public String getRole() {
        return identity.getRoles().stream().findFirst().orElse(null);
    }

    public boolean hasRole(String role) {
        return identity.hasRole(role);
    }

    public boolean isAnonymous() {
        return identity.isAnonymous();
    }

}
