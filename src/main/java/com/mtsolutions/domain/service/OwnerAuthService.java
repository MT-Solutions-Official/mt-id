package com.mtsolutions.domain.service;

import com.mtsolutions.application.common.ContextComponent;
import com.mtsolutions.application.client.google.GoogleTokenInfoResponseDto;
import com.mtsolutions.application.exception.ApplicationAuthenticationFailedException;
import com.mtsolutions.application.utils.DateUtils;
import com.mtsolutions.domain.constant.OwnerRole;
import com.mtsolutions.domain.dto.response.OwnerTokenResponseDto;
import com.mtsolutions.domain.entity.Owner;
import com.mtsolutions.domain.repository.OwnerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.UUID;

@ApplicationScoped
public class OwnerAuthService {

    @ConfigProperty(name = "app.mt.id.token.expiration.hours")
    Integer tokenExpirationInHours;

    @ConfigProperty(name = "app.mt.id.refresh-token.expiration.days")
    Integer refreshTokenExpirationInDays;

    @ConfigProperty(name = "app.mt.id.google.owner.audience")
    String googleOwnerAudience;

    private final OwnerRepository ownerRepository;
    private final BcryptService bcryptService;
    private final JwtService jwtService;
    private final OwnerRefreshTokenService ownerRefreshTokenService;
    private final GoogleTokenVerificationService googleTokenVerificationService;
    private final ContextComponent contextComponent;
    private final DateUtils dateUtils;

    public OwnerAuthService(OwnerRepository ownerRepository,
                            BcryptService bcryptService,
                            JwtService jwtService,
                            OwnerRefreshTokenService ownerRefreshTokenService,
                            GoogleTokenVerificationService googleTokenVerificationService,
                            ContextComponent contextComponent,
                            DateUtils dateUtils) {
        this.ownerRepository = ownerRepository;
        this.bcryptService = bcryptService;
        this.jwtService = jwtService;
        this.ownerRefreshTokenService = ownerRefreshTokenService;
        this.googleTokenVerificationService = googleTokenVerificationService;
        this.contextComponent = contextComponent;
        this.dateUtils = dateUtils;
    }

    public OwnerTokenResponseDto generateOwnerToken(String email, String password) {
        String normalizedEmail = normalize(email);
        if (normalizedEmail == null || password == null || password.isBlank()) {
            throw new ApplicationAuthenticationFailedException();
        }

        Owner owner = this.ownerRepository.findOwnerByEmail(normalizedEmail)
                .orElseThrow(ApplicationAuthenticationFailedException::new);

        if (Boolean.FALSE.equals(owner.getActive())
                || owner.getPassword() == null
                || owner.getPassword().getPassword() == null
                || !this.bcryptService.verifyPassword(password, owner.getPassword().getPassword())) {
            throw new ApplicationAuthenticationFailedException();
        }

        if (owner.getRole() == null) {
            owner.setRole(OwnerRole.OWNER_VIEWER);
            this.ownerRepository.persistOrUpdate(owner);
        }

        return this.issueTokens(owner);
    }

    public OwnerTokenResponseDto generateGoogleOwnerToken(String idToken) {
        GoogleTokenInfoResponseDto googleTokenInfo = this.verifyGoogleIdToken(idToken);

        Owner owner = this.ownerRepository.findOwnerByEmail(googleTokenInfo.getEmail())
                .orElseThrow(ApplicationAuthenticationFailedException::new);
        if (Boolean.FALSE.equals(owner.getActive())) {
            throw new ApplicationAuthenticationFailedException();
        }

        this.syncGoogleVerifiedEmail(owner);

        if (owner.getRole() == null) {
            owner.setRole(OwnerRole.OWNER_VIEWER);
            this.ownerRepository.persistOrUpdate(owner);
        }

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
        if (Boolean.FALSE.equals(owner.getActive())
                || owner.getEmail() == null
                || owner.getEmail().getEmail() == null
                || !email.equalsIgnoreCase(owner.getEmail().getEmail())) {
            throw new ApplicationAuthenticationFailedException();
        }

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
        Duration accessExpiration = Duration.ofHours(this.tokenExpirationInHours);
        Duration refreshExpiration = Duration.ofDays(resolveRefreshTokenExpirationDays());
        String refreshTokenId = UUID.randomUUID().toString();
        this.ownerRefreshTokenService.registerRefreshToken(refreshTokenId, owner.getOwnerId(), refreshExpiration);
        return this.jwtService.generateOwnerToken(owner, refreshTokenId, accessExpiration, refreshExpiration);
    }

    private GoogleTokenInfoResponseDto verifyGoogleIdToken(String idToken) {
        return this.googleTokenVerificationService.verifyIdToken(idToken, this.googleOwnerAudience);
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

    private String normalize(String value) {
        return value != null ? value.trim() : null;
    }
}
