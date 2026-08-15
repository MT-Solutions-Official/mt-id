package com.mtsolutions.application.filter;

import com.mtsolutions.application.cache.AccountStatusCache;
import com.mtsolutions.application.exception.AccountDisabledException;
import com.mtsolutions.application.utils.AccountStatusUtils;
import com.mtsolutions.domain.entity.ClientApplication;
import com.mtsolutions.domain.entity.Owner;
import com.mtsolutions.domain.entity.User;
import com.mtsolutions.domain.repository.ClientApplicationRepository;
import com.mtsolutions.domain.repository.OwnerRepository;
import com.mtsolutions.domain.repository.UserRepository;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Provider
@Priority(Priorities.AUTHORIZATION - 20)
public class DisabledAccountFilter implements ContainerRequestFilter {

    private final SecurityIdentity identity;
    private final JsonWebToken jwt;
    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final ClientApplicationRepository clientApplicationRepository;
    private final AccountStatusCache accountStatusCache;

    public DisabledAccountFilter(SecurityIdentity identity,
                                 JsonWebToken jwt,
                                 UserRepository userRepository,
                                 OwnerRepository ownerRepository,
                                 ClientApplicationRepository clientApplicationRepository,
                                 AccountStatusCache accountStatusCache) {
        this.identity = identity;
        this.jwt = jwt;
        this.userRepository = userRepository;
        this.ownerRepository = ownerRepository;
        this.clientApplicationRepository = clientApplicationRepository;
        this.accountStatusCache = accountStatusCache;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (this.identity.isAnonymous()) {
            return;
        }

        String path = requestContext.getUriInfo().getPath();
        if (path != null && (path.endsWith("/auth/users/logout") || path.endsWith("/auth/owners/logout"))) {
            return;
        }

        if (this.identity.getRoles().contains("USER")) {
            this.ensureUserEnabled();
            return;
        }
        if (this.identity.getRoles().contains("OWNER_WRITER") || this.identity.getRoles().contains("OWNER_VIEWER")) {
            this.ensureOwnerEnabled();
            return;
        }
        if (this.identity.getRoles().contains("APPLICATION")) {
            this.ensureApplicationEnabled();
        }
    }

    private void ensureUserEnabled() {
        String userId = this.jwt.getClaim("userId");
        if (userId == null || userId.isBlank()) {
            throw new AccountDisabledException();
        }
        boolean disabled = this.accountStatusCache.getUserDisabled(userId).orElseGet(() -> {
            User user = this.userRepository.findUserById(userId);
            boolean value = AccountStatusUtils.isDisabled(user);
            this.accountStatusCache.putUserDisabled(userId, value);
            return value;
        });
        if (disabled) {
            throw new AccountDisabledException();
        }
    }

    private void ensureOwnerEnabled() {
        String ownerId = this.jwt.getClaim("ownerId");
        if (ownerId == null || ownerId.isBlank()) {
            throw new AccountDisabledException();
        }
        boolean disabled = this.accountStatusCache.getOwnerDisabled(ownerId).orElseGet(() -> {
            Owner owner = this.ownerRepository.findOwnerById(ownerId);
            boolean value = AccountStatusUtils.isDisabled(owner);
            this.accountStatusCache.putOwnerDisabled(ownerId, value);
            return value;
        });
        if (disabled) {
            throw new AccountDisabledException();
        }
    }

    private void ensureApplicationEnabled() {
        String appId = this.jwt.getClaim("app_id");
        if (appId == null || appId.isBlank()) {
            throw new AccountDisabledException();
        }
        boolean disabled = this.accountStatusCache.getApplicationDisabled(appId).orElseGet(() -> {
            ClientApplication application = this.clientApplicationRepository.findClientApplicationById(appId);
            boolean value = Boolean.FALSE.equals(application.getActive());
            this.accountStatusCache.putApplicationDisabled(appId, value);
            return value;
        });
        if (disabled) {
            throw new AccountDisabledException();
        }
    }
}
