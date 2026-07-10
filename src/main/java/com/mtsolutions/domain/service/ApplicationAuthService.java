package com.mtsolutions.domain.service;

import com.mtsolutions.application.exception.ApplicationAuthenticationFailedException;
import com.mtsolutions.application.common.ContextComponent;
import com.mtsolutions.domain.constant.OwnerRole;
import com.mtsolutions.domain.dto.response.AppTokenResponseDto;
import com.mtsolutions.domain.dto.response.OwnerTokenResponseDto;
import com.mtsolutions.domain.dto.response.UserTokenResponseDto;
import com.mtsolutions.domain.entity.ClientApplication;
import com.mtsolutions.domain.entity.Owner;
import com.mtsolutions.domain.entity.User;
import com.mtsolutions.domain.model.Email;
import com.mtsolutions.domain.repository.ClientApplicationRepository;
import com.mtsolutions.domain.repository.OwnerRepository;
import com.mtsolutions.domain.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.UUID;

@ApplicationScoped
public class ApplicationAuthService {

    @ConfigProperty(name = "app.mt.id.token.expiration.hours")
    Integer tokenExpirationInHours;

    @ConfigProperty(name = "app.mt.id.refresh-token.expiration.days")
    Integer refreshTokenExpirationInDays;

    private final OwnerRepository ownerRepository;
    private final ClientApplicationRepository clientApplicationRepository;
    private final UserRepository userRepository;
    private final BcryptService bcryptService;
    private final JwtService jwtService;
    private final OwnerRefreshTokenService ownerRefreshTokenService;
    private final UserRefreshTokenService userRefreshTokenService;
    private final ContextComponent contextComponent;

    public ApplicationAuthService(OwnerRepository ownerRepository,
                                  ClientApplicationRepository clientApplicationRepository,
                                  UserRepository userRepository,
                                  BcryptService bcryptService,
                                  JwtService jwtService,
                                  OwnerRefreshTokenService ownerRefreshTokenService,
                                  UserRefreshTokenService userRefreshTokenService,
                                  ContextComponent contextComponent) {
        this.ownerRepository = ownerRepository;
        this.clientApplicationRepository = clientApplicationRepository;
        this.userRepository = userRepository;
        this.bcryptService = bcryptService;
        this.jwtService = jwtService;
        this.ownerRefreshTokenService = ownerRefreshTokenService;
        this.userRefreshTokenService = userRefreshTokenService;
        this.contextComponent = contextComponent;
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

        Duration accessExpiration = Duration.ofHours(this.tokenExpirationInHours);
        Duration refreshExpiration = Duration.ofDays(resolveRefreshTokenExpirationDays());
        String refreshTokenId = UUID.randomUUID().toString();
        this.ownerRefreshTokenService.registerRefreshToken(refreshTokenId, owner.getOwnerId(), refreshExpiration);
        return this.jwtService.generateOwnerToken(owner, refreshTokenId, accessExpiration, refreshExpiration);
    }

    public AppTokenResponseDto generateApplicationToken(String apiKey, String apiSecret) {
        String normalizedApiKey = normalize(apiKey);
        if (normalizedApiKey == null || apiSecret == null || apiSecret.isBlank()) {
            throw new ApplicationAuthenticationFailedException();
        }

        ClientApplication clientApplication = this.clientApplicationRepository.findClientApplicationByApiKey(normalizedApiKey)
                .orElseThrow(ApplicationAuthenticationFailedException::new);

        if (Boolean.FALSE.equals(clientApplication.getActive())
                || clientApplication.getApiSecret() == null
                || !this.bcryptService.verifyPassword(apiSecret, clientApplication.getApiSecret())) {
            throw new ApplicationAuthenticationFailedException();
        }

        Duration expiration = Duration.ofMinutes(resolveApplicationJwtExpiration(clientApplication));
        return this.jwtService.generateApplicationToken(clientApplication, expiration);
    }

    public UserTokenResponseDto generateUserToken(String email, String password) {
        String normalizedEmail = normalize(email);
        if (normalizedEmail == null || password == null || password.isBlank()) {
            throw new ApplicationAuthenticationFailedException();
        }

        User user = this.userRepository.findUserByEmail(normalizedEmail);
        Email loginEmail = this.resolveLoginEmail(user, normalizedEmail);

        if (Boolean.FALSE.equals(user.getActive())
                || user.getPassword() == null
                || user.getPassword().getPassword() == null
                || !this.bcryptService.verifyPassword(password, user.getPassword().getPassword())) {
            throw new ApplicationAuthenticationFailedException();
        }

        ClientApplication clientApplication = this.clientApplicationRepository.findClientApplicationById(user.getAppId());
        Duration accessExpiration = Duration.ofMinutes(resolveApplicationJwtExpiration(clientApplication));
        Duration refreshExpiration = Duration.ofDays(resolveRefreshTokenExpirationDays(clientApplication));
        String refreshTokenId = UUID.randomUUID().toString();
        this.userRefreshTokenService.registerRefreshToken(refreshTokenId, user.getUserId(), clientApplication.getAppId(), refreshExpiration);

        return this.jwtService.generateUserToken(user, loginEmail, refreshTokenId, accessExpiration, refreshExpiration);
    }

    public OwnerTokenResponseDto refreshOwnerToken() {
        String tokenType = this.contextComponent.getAuthenticatedTokenTypeOrNull();
        String role = this.contextComponent.getRole();
        String ownerId = this.contextComponent.getAuthenticatedOwnerId();
        String email = this.contextComponent.getEmail();

        if (!"REFRESH_TOKEN".equals(role)
                || !"refresh".equalsIgnoreCase(tokenType)
                || email == null || email.isBlank()) {
            throw new ApplicationAuthenticationFailedException();
        }

        Owner owner = this.ownerRepository.findOwnerById(ownerId);
        if (Boolean.FALSE.equals(owner.getActive())
                || owner.getEmail() == null
                || owner.getEmail().getEmail() == null
                || !email.equalsIgnoreCase(owner.getEmail().getEmail())) {
            throw new ApplicationAuthenticationFailedException();
        }

        String refreshTokenId = this.contextComponent.getAuthenticatedTokenIdOrNull();
        if (refreshTokenId == null || refreshTokenId.isBlank()) {
            throw new ApplicationAuthenticationFailedException();
        }

        this.ownerRefreshTokenService.validateActiveRefreshToken(refreshTokenId, ownerId);
        this.ownerRefreshTokenService.revokeRefreshToken(refreshTokenId);

        Duration accessExpiration = Duration.ofHours(this.tokenExpirationInHours);
        Duration refreshExpiration = Duration.ofDays(resolveRefreshTokenExpirationDays());
        String newRefreshTokenId = UUID.randomUUID().toString();
        this.ownerRefreshTokenService.registerRefreshToken(newRefreshTokenId, ownerId, refreshExpiration);
        return this.jwtService.generateOwnerToken(owner, newRefreshTokenId, accessExpiration, refreshExpiration);
    }

    public UserTokenResponseDto refreshUserToken() {
        String tokenType = this.contextComponent.getAuthenticatedTokenTypeOrNull();
        String role = this.contextComponent.getRole();
        String userId = this.contextComponent.getAuthenticatedUserIdOrNull();
        String appId = this.contextComponent.getAuthenticatedAppIdOrNull();
        String email = this.contextComponent.getEmail();
        String refreshTokenId = this.contextComponent.getAuthenticatedTokenIdOrNull();

        if (!"REFRESH_TOKEN".equals(role)
                || !"refresh".equalsIgnoreCase(tokenType)
                || userId == null || userId.isBlank()
                || appId == null || appId.isBlank()
                || refreshTokenId == null || refreshTokenId.isBlank()
                || email == null || email.isBlank()) {
            throw new ApplicationAuthenticationFailedException();
        }

        User user = this.userRepository.findUserById(userId);
        if (Boolean.FALSE.equals(user.getActive())
                || user.getAppId() == null
                || !appId.equals(user.getAppId())) {
            throw new ApplicationAuthenticationFailedException();
        }

        ClientApplication clientApplication = this.clientApplicationRepository.findClientApplicationById(appId);
        Email loginEmail = this.resolveRefreshEmail(user, email);

        this.userRefreshTokenService.validateActiveRefreshToken(refreshTokenId, userId, appId);
        this.userRefreshTokenService.revokeRefreshToken(refreshTokenId);

        Duration accessExpiration = Duration.ofMinutes(resolveApplicationJwtExpiration(clientApplication));
        Duration refreshExpiration = Duration.ofDays(resolveRefreshTokenExpirationDays(clientApplication));
        String newRefreshTokenId = UUID.randomUUID().toString();
        this.userRefreshTokenService.registerRefreshToken(newRefreshTokenId, userId, appId, refreshExpiration);

        return this.jwtService.generateUserToken(user, loginEmail, newRefreshTokenId, accessExpiration, refreshExpiration);
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

    public void logoutUser() {
        String tokenType = this.contextComponent.getAuthenticatedTokenTypeOrNull();
        String role = this.contextComponent.getRole();
        String userId = this.contextComponent.getAuthenticatedUserIdOrNull();
        String appId = this.contextComponent.getAuthenticatedAppIdOrNull();
        String refreshTokenId = this.contextComponent.getAuthenticatedTokenIdOrNull();

        if (!"REFRESH_TOKEN".equals(role)
                || !"refresh".equalsIgnoreCase(tokenType)
                || userId == null || userId.isBlank()
                || appId == null || appId.isBlank()
                || refreshTokenId == null || refreshTokenId.isBlank()) {
            throw new ApplicationAuthenticationFailedException();
        }

        this.userRefreshTokenService.validateActiveRefreshToken(refreshTokenId, userId, appId);
        this.userRefreshTokenService.revokeRefreshToken(refreshTokenId);
    }

    private Email resolveLoginEmail(User user, String requestedEmail) {
        if (user.getEmails() == null || user.getEmails().isEmpty()) {
            throw new ApplicationAuthenticationFailedException();
        }

        Email primaryEmail = user.getEmails().stream()
                .filter(email -> email != null && Boolean.TRUE.equals(email.getPrimary()))
                .findFirst()
                .orElse(null);

        if (primaryEmail != null) {
            if (primaryEmail.getEmail() == null || !requestedEmail.equalsIgnoreCase(primaryEmail.getEmail())) {
                throw new ApplicationAuthenticationFailedException();
            }
            return primaryEmail;
        }

        return user.getEmails().stream()
                .filter(email -> email != null
                        && email.getEmail() != null
                        && requestedEmail.equalsIgnoreCase(email.getEmail()))
                .findFirst()
                .orElseThrow(ApplicationAuthenticationFailedException::new);
    }

    private long resolveApplicationJwtExpiration(ClientApplication clientApplication) {
        if (clientApplication.getJwtExpirationInMinutes() != null && clientApplication.getJwtExpirationInMinutes() > 0) {
            return clientApplication.getJwtExpirationInMinutes();
        }

        return this.tokenExpirationInHours * 60L;
    }

    private long resolveRefreshTokenExpirationDays() {
        if (this.refreshTokenExpirationInDays != null && this.refreshTokenExpirationInDays > 0) {
            return this.refreshTokenExpirationInDays;
        }

        return 30L;
    }

    private long resolveRefreshTokenExpirationDays(ClientApplication clientApplication) {
        if (clientApplication.getRefreshTokenExpirationInDays() != null && clientApplication.getRefreshTokenExpirationInDays() > 0) {
            return clientApplication.getRefreshTokenExpirationInDays();
        }

        return resolveRefreshTokenExpirationDays();
    }

    private Email resolveRefreshEmail(User user, String requestedEmail) {
        if (user.getEmails() == null || user.getEmails().isEmpty()) {
            throw new ApplicationAuthenticationFailedException();
        }

        return user.getEmails().stream()
                .filter(email -> email != null
                        && email.getEmail() != null
                        && requestedEmail.equalsIgnoreCase(email.getEmail()))
                .findFirst()
                .orElseThrow(ApplicationAuthenticationFailedException::new);
    }

    private String normalize(String value) {
        return value != null ? value.trim() : null;
    }
}
