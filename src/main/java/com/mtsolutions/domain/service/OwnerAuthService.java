package com.mtsolutions.domain.service;

import com.mtsolutions.application.common.ClientRequestContext;
import com.mtsolutions.application.common.ContextComponent;
import com.mtsolutions.application.client.google.GoogleTokenInfoResponseDto;
import com.mtsolutions.application.exception.AccountDisabledException;
import com.mtsolutions.application.exception.ApplicationAuthenticationFailedException;
import com.mtsolutions.application.exception.EmailNotVerifiedException;
import com.mtsolutions.application.exception.TooManyRequestsException;
import com.mtsolutions.application.utils.AccountStatusUtils;
import com.mtsolutions.application.utils.DateUtils;
import com.mtsolutions.application.utils.NormalizeUtils;
import com.mtsolutions.domain.dto.response.OwnerTokenResponseDto;
import com.mtsolutions.domain.entity.Owner;
import com.mtsolutions.domain.repository.OwnerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.UUID;

@ApplicationScoped
public class OwnerAuthService {

    @ConfigProperty(name = "app.mt.id.token.expiration.minutes")
    Integer tokenExpirationMinutes;

    @ConfigProperty(name = "app.mt.id.refresh-token.expiration.days")
    Integer refreshTokenExpirationInDays;

    @ConfigProperty(name = "app.mt.id.google.owner.audience")
    String googleOwnerAudience;

    @ConfigProperty(name = "app.mt.id.throttle.login.max-attempts")
    Integer loginMaxAttempts;

    @ConfigProperty(name = "app.mt.id.throttle.login.window.seconds")
    Integer loginWindowSeconds;

    @ConfigProperty(name = "app.mt.id.throttle.login.min-interval.seconds")
    Integer loginMinIntervalSeconds;

    @ConfigProperty(name = "app.mt.id.throttle.login.ip.max-attempts")
    Integer loginIpMaxAttempts;

    @ConfigProperty(name = "app.mt.id.throttle.login.ip.window.seconds")
    Integer loginIpWindowSeconds;

    @ConfigProperty(name = "app.mt.id.throttle.login.ip.min-interval.seconds")
    Integer loginIpMinIntervalSeconds;

    private final OwnerRepository ownerRepository;
    private final BcryptService bcryptService;
    private final JwtService jwtService;
    private final OwnerRefreshTokenService ownerRefreshTokenService;
    private final GoogleTokenVerificationService googleTokenVerificationService;
    private final ContextComponent contextComponent;
    private final ClientRequestContext clientRequestContext;
    private final DateUtils dateUtils;
    private final RequestThrottleService requestThrottleService;

    public OwnerAuthService(OwnerRepository ownerRepository,
                            BcryptService bcryptService,
                            JwtService jwtService,
                            OwnerRefreshTokenService ownerRefreshTokenService,
                            GoogleTokenVerificationService googleTokenVerificationService,
                            ContextComponent contextComponent,
                            ClientRequestContext clientRequestContext,
                            DateUtils dateUtils,
                            RequestThrottleService requestThrottleService) {
        this.ownerRepository = ownerRepository;
        this.bcryptService = bcryptService;
        this.jwtService = jwtService;
        this.ownerRefreshTokenService = ownerRefreshTokenService;
        this.googleTokenVerificationService = googleTokenVerificationService;
        this.contextComponent = contextComponent;
        this.clientRequestContext = clientRequestContext;
        this.dateUtils = dateUtils;
        this.requestThrottleService = requestThrottleService;
    }

    public OwnerTokenResponseDto generateOwnerToken(String email, String password) {
        String normalizedEmail = NormalizeUtils.normalizeEmail(email);
        if (normalizedEmail == null || password == null || password.isBlank()) {
            throw new ApplicationAuthenticationFailedException();
        }

        this.ensureLoginNotThrottled("owner-login", normalizedEmail);

        try {
            Owner owner = this.ownerRepository.findOwnerByEmail(normalizedEmail)
                    .orElseThrow(ApplicationAuthenticationFailedException::new);

            if (owner.getPassword() == null
                    || owner.getPassword().getPassword() == null
                    || !this.bcryptService.verifyPassword(password, owner.getPassword().getPassword())) {
                throw new ApplicationAuthenticationFailedException();
            }

            this.ensureAccountEnabled(owner);
            this.ensureEmailVerified(owner);

            this.requestThrottleService.clear("owner-login", normalizedEmail);
            return this.issueTokens(owner);
        } catch (ApplicationAuthenticationFailedException e) {
            this.recordLoginFailure("owner-login", normalizedEmail);
            throw e;
        }
    }

    public OwnerTokenResponseDto generateGoogleOwnerToken(String idToken, String nonce) {
        this.ensureIpNotThrottled("owner-login-ip");
        GoogleTokenInfoResponseDto googleTokenInfo = this.googleTokenVerificationService.verifyIdToken(
                idToken,
                this.googleOwnerAudience,
                nonce
        );

        String normalizedEmail = NormalizeUtils.normalizeEmail(googleTokenInfo.getEmail());
        if (normalizedEmail == null) {
            throw new ApplicationAuthenticationFailedException();
        }

        Owner owner = this.ownerRepository.findOwnerByEmail(normalizedEmail)
                .orElseThrow(ApplicationAuthenticationFailedException::new);
        this.ensureAccountEnabled(owner);

        this.syncGoogleVerifiedEmail(owner);

        return this.issueTokens(owner);
    }

    public OwnerTokenResponseDto refreshOwnerToken() {
        String tokenType = this.contextComponent.getAuthenticatedTokenTypeOrNull();
        String role = this.contextComponent.getRole();
        String ownerId = this.contextComponent.getAuthenticatedOwnerId();
        String email = this.contextComponent.getEmail();
        String refreshTokenId = this.contextComponent.getAuthenticatedTokenIdOrNull();

        if (!"REFRESH_TOKEN".equals(role)
                || !"refresh".equalsIgnoreCase(tokenType)
                || email == null || email.isBlank()
                || refreshTokenId == null || refreshTokenId.isBlank()) {
            throw new ApplicationAuthenticationFailedException();
        }

        Owner owner = this.ownerRepository.findOwnerById(ownerId);
        if (owner.getEmail() == null
                || owner.getEmail().getEmail() == null
                || !email.equalsIgnoreCase(owner.getEmail().getEmail())) {
            throw new ApplicationAuthenticationFailedException();
        }
        this.ensureAccountEnabled(owner);

        this.ownerRefreshTokenService.validateActiveRefreshToken(refreshTokenId, ownerId);
        this.ownerRefreshTokenService.revokeRefreshToken(refreshTokenId);
        return this.issueTokens(owner);
    }

    public void logoutOwner() {
        String tokenType = this.contextComponent.getAuthenticatedTokenTypeOrNull();
        String role = this.contextComponent.getRole();
        String refreshTokenId = this.contextComponent.getAuthenticatedTokenIdOrNull();
        String ownerId = this.contextComponent.getAuthenticatedOwnerId();

        if (!"REFRESH_TOKEN".equals(role)
                || !"refresh".equalsIgnoreCase(tokenType)
                || refreshTokenId == null || refreshTokenId.isBlank()) {
            throw new ApplicationAuthenticationFailedException();
        }

        this.ownerRefreshTokenService.validateActiveRefreshToken(refreshTokenId, ownerId);
        this.ownerRefreshTokenService.revokeRefreshToken(refreshTokenId);
    }

    private OwnerTokenResponseDto issueTokens(Owner owner) {
        Duration accessExpiration = Duration.ofMinutes(this.resolveDefaultAccessMinutes());
        Duration refreshExpiration = Duration.ofDays(resolveRefreshTokenExpirationDays());
        String refreshTokenId = UUID.randomUUID().toString();
        this.ownerRefreshTokenService.registerRefreshToken(refreshTokenId, owner.getOwnerId(), refreshExpiration);
        return this.jwtService.generateOwnerToken(owner, refreshTokenId, accessExpiration, refreshExpiration);
    }

    private void ensureEmailVerified(Owner owner) {
        if (owner.getEmail() == null || !Boolean.TRUE.equals(owner.getEmail().getVerified())) {
            throw new EmailNotVerifiedException();
        }
    }

    private void ensureAccountEnabled(Owner owner) {
        if (AccountStatusUtils.isDisabled(owner)) {
            throw new AccountDisabledException();
        }
    }

    private void ensureLoginNotThrottled(String action, String identifier) {
        this.ensureIpNotThrottled("owner-login-ip");
        if (this.requestThrottleService.isThrottled(
                action,
                identifier,
                resolveLimit(this.loginMaxAttempts, 10),
                Duration.ofSeconds(resolveLimit(this.loginWindowSeconds, 900)),
                Duration.ofSeconds(resolveLimit(this.loginMinIntervalSeconds, 1)))) {
            throw new TooManyRequestsException();
        }
    }

    private void ensureIpNotThrottled(String action) {
        if (this.requestThrottleService.isThrottled(
                action,
                this.clientRequestContext.clientIp(),
                resolveLimit(this.loginIpMaxAttempts, 30),
                Duration.ofSeconds(resolveLimit(this.loginIpWindowSeconds, 900)),
                Duration.ofSeconds(resolveLimit(this.loginIpMinIntervalSeconds, 1)))) {
            throw new TooManyRequestsException();
        }
    }

    private void recordLoginFailure(String action, String identifier) {
        this.requestThrottleService.recordAttempt(
                action,
                identifier,
                Duration.ofSeconds(resolveLimit(this.loginWindowSeconds, 900))
        );
        this.requestThrottleService.recordAttempt(
                "owner-login-ip",
                this.clientRequestContext.clientIp(),
                Duration.ofSeconds(resolveLimit(this.loginIpWindowSeconds, 900))
        );
    }

    private int resolveLimit(Integer configuredValue, int fallback) {
        return configuredValue != null && configuredValue > 0 ? configuredValue : fallback;
    }

    private void syncGoogleVerifiedEmail(Owner owner) {
        if (owner.getEmail() == null || Boolean.TRUE.equals(owner.getEmail().getVerified())) {
            return;
        }

        owner.getEmail().setVerified(true);
        owner.getEmail().setVerificationToken(null);
        owner.getEmail().setVerificationTokenExpiry(null);
        owner.setUpdatedAt(this.dateUtils.now());
        this.ownerRepository.persistOrUpdate(owner);
    }

    private long resolveRefreshTokenExpirationDays() {
        if (this.refreshTokenExpirationInDays != null && this.refreshTokenExpirationInDays > 0) {
            return this.refreshTokenExpirationInDays;
        }

        return 30L;
    }

    private long resolveDefaultAccessMinutes() {
        if (this.tokenExpirationMinutes != null && this.tokenExpirationMinutes > 0) {
            return this.tokenExpirationMinutes;
        }
        return 15L;
    }
}
