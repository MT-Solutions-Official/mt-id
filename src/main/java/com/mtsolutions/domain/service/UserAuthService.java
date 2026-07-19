package com.mtsolutions.domain.service;

import com.mtsolutions.application.common.ContextComponent;
import com.mtsolutions.application.client.google.GoogleTokenInfoResponseDto;
import com.mtsolutions.application.exception.ApplicationAuthenticationFailedException;
import com.mtsolutions.application.utils.DateUtils;
import com.mtsolutions.domain.dto.response.UserTokenResponseDto;
import com.mtsolutions.domain.entity.ClientApplication;
import com.mtsolutions.domain.entity.User;
import com.mtsolutions.domain.model.Email;
import com.mtsolutions.domain.repository.ClientApplicationRepository;
import com.mtsolutions.domain.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.UUID;

@ApplicationScoped
public class UserAuthService {

    @ConfigProperty(name = "app.mt.id.refresh-token.expiration.days")
    Integer refreshTokenExpirationInDays;

    @ConfigProperty(name = "app.mt.id.google.user.audience")
    String googleUserAudience;

    private final UserRepository userRepository;
    private final ClientApplicationRepository clientApplicationRepository;
    private final BcryptService bcryptService;
    private final JwtService jwtService;
    private final UserRefreshTokenService userRefreshTokenService;
    private final GoogleTokenVerificationService googleTokenVerificationService;
    private final ContextComponent contextComponent;
    private final DateUtils dateUtils;

    public UserAuthService(UserRepository userRepository,
                           ClientApplicationRepository clientApplicationRepository,
                           BcryptService bcryptService,
                           JwtService jwtService,
                           UserRefreshTokenService userRefreshTokenService,
                           GoogleTokenVerificationService googleTokenVerificationService,
                           ContextComponent contextComponent,
                           DateUtils dateUtils) {
        this.userRepository = userRepository;
        this.clientApplicationRepository = clientApplicationRepository;
        this.bcryptService = bcryptService;
        this.jwtService = jwtService;
        this.userRefreshTokenService = userRefreshTokenService;
        this.googleTokenVerificationService = googleTokenVerificationService;
        this.contextComponent = contextComponent;
        this.dateUtils = dateUtils;
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

        return this.issueTokens(user, loginEmail);
    }

    public UserTokenResponseDto generateGoogleUserToken(String idToken) {
        GoogleTokenInfoResponseDto googleTokenInfo = this.verifyGoogleIdToken(idToken);

        User user = this.userRepository.findUserByEmail(googleTokenInfo.getEmail());
        Email loginEmail = this.resolveLoginEmail(user, googleTokenInfo.getEmail());
        if (Boolean.FALSE.equals(user.getActive())) {
            throw new ApplicationAuthenticationFailedException();
        }

        this.syncGoogleVerifiedEmail(user, loginEmail);
        return this.issueTokens(user, loginEmail);
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

    private UserTokenResponseDto issueTokens(User user, Email loginEmail) {
        ClientApplication clientApplication = this.clientApplicationRepository.findClientApplicationById(user.getAppId());
        Duration accessExpiration = Duration.ofMinutes(resolveApplicationJwtExpiration(clientApplication));
        Duration refreshExpiration = Duration.ofDays(resolveRefreshTokenExpirationDays(clientApplication));
        String refreshTokenId = UUID.randomUUID().toString();
        this.userRefreshTokenService.registerRefreshToken(refreshTokenId, user.getUserId(), clientApplication.getAppId(), refreshExpiration);

        return this.jwtService.generateUserToken(user, loginEmail, refreshTokenId, accessExpiration, refreshExpiration);
    }

    private GoogleTokenInfoResponseDto verifyGoogleIdToken(String idToken) {
        return this.googleTokenVerificationService.verifyIdToken(idToken, this.googleUserAudience);
    }

    private void syncGoogleVerifiedEmail(User user, Email loginEmail) {
        if (Boolean.TRUE.equals(loginEmail.getVerified())) {
            return;
        }

        loginEmail.setVerified(true);
        loginEmail.setVerificationToken(null);
        loginEmail.setVerificationTokenExpiry(null);
        user.setUpdatedAt(this.dateUtils.now());
        this.userRepository.persistOrUpdate(user);
    }

    private long resolveApplicationJwtExpiration(ClientApplication clientApplication) {
        if (clientApplication.getJwtExpirationInMinutes() != null && clientApplication.getJwtExpirationInMinutes() > 0) {
            return clientApplication.getJwtExpirationInMinutes();
        }

        return 24L * 60L;
    }

    private long resolveRefreshTokenExpirationDays(ClientApplication clientApplication) {
        if (clientApplication.getRefreshTokenExpirationInDays() != null && clientApplication.getRefreshTokenExpirationInDays() > 0) {
            return clientApplication.getRefreshTokenExpirationInDays();
        }

        return resolveRefreshTokenExpirationDays();
    }

    private long resolveRefreshTokenExpirationDays() {
        if (this.refreshTokenExpirationInDays != null && this.refreshTokenExpirationInDays > 0) {
            return this.refreshTokenExpirationInDays;
        }

        return 30L;
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
