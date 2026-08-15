package com.mtsolutions.domain.service;

import com.mtsolutions.application.common.ClientRequestContext;
import com.mtsolutions.application.common.ContextComponent;
import com.mtsolutions.application.client.google.GoogleTokenInfoResponseDto;
import com.mtsolutions.application.exception.AccountDisabledException;
import com.mtsolutions.application.exception.ApplicationAuthenticationFailedException;
import com.mtsolutions.application.exception.ClientApplicationNotFoundException;
import com.mtsolutions.application.exception.EmailAlreadyExistsException;
import com.mtsolutions.application.exception.EmailNotVerifiedException;
import com.mtsolutions.application.exception.TooManyRequestsException;
import com.mtsolutions.application.utils.AccountStatusUtils;
import com.mtsolutions.application.utils.DateUtils;
import com.mtsolutions.application.utils.NormalizeUtils;
import com.mtsolutions.domain.dto.request.GenerateUserGoogleTokenRequestDto;
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

    @ConfigProperty(name = "app.mt.id.token.expiration.minutes")
    Integer tokenExpirationMinutes;

    @ConfigProperty(name = "app.mt.id.refresh-token.expiration.days")
    Integer refreshTokenExpirationInDays;

    @ConfigProperty(name = "app.mt.id.google.user.audience")
    String googleUserAudience;

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

    private final UserRepository userRepository;
    private final ClientApplicationRepository clientApplicationRepository;
    private final BcryptService bcryptService;
    private final JwtService jwtService;
    private final UserRefreshTokenService userRefreshTokenService;
    private final GoogleTokenVerificationService googleTokenVerificationService;
    private final ContextComponent contextComponent;
    private final ClientRequestContext clientRequestContext;
    private final DateUtils dateUtils;
    private final RequestThrottleService requestThrottleService;
    private final UserService userService;
    private final UserRoleService userRoleService;

    public UserAuthService(UserRepository userRepository,
                           ClientApplicationRepository clientApplicationRepository,
                           BcryptService bcryptService,
                           JwtService jwtService,
                           UserRefreshTokenService userRefreshTokenService,
                           GoogleTokenVerificationService googleTokenVerificationService,
                           ContextComponent contextComponent,
                           ClientRequestContext clientRequestContext,
                           DateUtils dateUtils,
                           RequestThrottleService requestThrottleService,
                           UserService userService,
                           UserRoleService userRoleService) {
        this.userRepository = userRepository;
        this.clientApplicationRepository = clientApplicationRepository;
        this.bcryptService = bcryptService;
        this.jwtService = jwtService;
        this.userRefreshTokenService = userRefreshTokenService;
        this.googleTokenVerificationService = googleTokenVerificationService;
        this.contextComponent = contextComponent;
        this.clientRequestContext = clientRequestContext;
        this.dateUtils = dateUtils;
        this.requestThrottleService = requestThrottleService;
        this.userService = userService;
        this.userRoleService = userRoleService;
    }

    public UserTokenResponseDto generateUserToken(String email, String password, String appId) {
        String normalizedEmail = NormalizeUtils.normalizeEmail(email);
        String normalizedAppId = NormalizeUtils.trimToNull(appId);
        if (normalizedEmail == null || password == null || password.isBlank() || normalizedAppId == null) {
            throw new ApplicationAuthenticationFailedException();
        }

        String throttleId = normalizedAppId + ":" + normalizedEmail;
        this.ensureLoginNotThrottled("user-login", throttleId);

        try {
            this.requireActiveApplication(normalizedAppId);
            User user = this.userRepository.findUserByAppIdAndEmail(normalizedAppId, normalizedEmail)
                    .orElseThrow(ApplicationAuthenticationFailedException::new);
            Email loginEmail = this.resolveLoginEmail(user, normalizedEmail);

            if (user.getPassword() == null
                    || user.getPassword().getPassword() == null
                    || !this.bcryptService.verifyPassword(password, user.getPassword().getPassword())) {
                throw new ApplicationAuthenticationFailedException();
            }

            this.ensureAccountEnabled(user);
            this.ensureEmailVerified(loginEmail);
            this.requestThrottleService.clear("user-login", throttleId);
            return this.issueTokens(user, loginEmail);
        } catch (ApplicationAuthenticationFailedException e) {
            this.recordLoginFailure("user-login", throttleId);
            throw e;
        }
    }

    public UserTokenResponseDto generateGoogleUserToken(GenerateUserGoogleTokenRequestDto request) {
        String normalizedAppId = NormalizeUtils.trimToNull(request != null ? request.appId() : null);
        if (normalizedAppId == null || request == null) {
            throw new ApplicationAuthenticationFailedException();
        }

        this.ensureIpNotThrottled("user-login-ip");

        ClientApplication clientApplication = this.requireActiveApplication(normalizedAppId);

        String audience = NormalizeUtils.trimToNull(clientApplication.getGoogleAudience());
        if (audience == null) {
            audience = this.googleUserAudience;
        }
        GoogleTokenInfoResponseDto googleTokenInfo = this.googleTokenVerificationService.verifyIdToken(
                request.idToken(),
                audience,
                request.nonce()
        );
        String normalizedEmail = NormalizeUtils.normalizeEmail(googleTokenInfo.getEmail());
        if (normalizedEmail == null) {
            throw new ApplicationAuthenticationFailedException();
        }

        User user;
        try {
            user = this.userRepository.findUserByAppIdAndEmail(normalizedAppId, normalizedEmail)
                    .orElseGet(() -> this.userService.provisionGoogleUser(
                            normalizedAppId,
                            normalizedEmail,
                            googleTokenInfo.getName(),
                            request
                    ));
        } catch (EmailAlreadyExistsException e) {
            user = this.userRepository.findUserByAppIdAndEmail(normalizedAppId, normalizedEmail)
                    .orElseThrow(ApplicationAuthenticationFailedException::new);
        } catch (ClientApplicationNotFoundException e) {
            throw new ApplicationAuthenticationFailedException();
        }
        Email loginEmail = this.resolveLoginEmail(user, normalizedEmail);
        this.ensureAccountEnabled(user);

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
        if (user.getAppId() == null || !appId.equals(user.getAppId())) {
            throw new ApplicationAuthenticationFailedException();
        }
        this.ensureAccountEnabled(user);
        ClientApplication clientApplication = this.requireActiveApplication(appId);
        Email loginEmail = this.resolveRefreshEmail(user, email);

        this.userRefreshTokenService.validateActiveRefreshToken(refreshTokenId, userId, appId);
        this.userRefreshTokenService.revokeRefreshToken(refreshTokenId);

        Duration accessExpiration = Duration.ofMinutes(resolveApplicationJwtExpiration(clientApplication));
        Duration refreshExpiration = Duration.ofDays(resolveRefreshTokenExpirationDays(clientApplication));
        String newRefreshTokenId = UUID.randomUUID().toString();
        this.userRefreshTokenService.registerRefreshToken(newRefreshTokenId, userId, appId, refreshExpiration);

        return this.jwtService.generateUserToken(
                user,
                loginEmail,
                newRefreshTokenId,
                accessExpiration,
                refreshExpiration,
                this.userRoleService.resolveRoleNamesByIds(user.getRoleIds())
        );
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

        return this.jwtService.generateUserToken(
                user,
                loginEmail,
                refreshTokenId,
                accessExpiration,
                refreshExpiration,
                this.userRoleService.resolveRoleNamesByIds(user.getRoleIds())
        );
    }

    private void ensureEmailVerified(Email loginEmail) {
        if (!Boolean.TRUE.equals(loginEmail.getVerified())) {
            throw new EmailNotVerifiedException();
        }
    }

    private void ensureAccountEnabled(User user) {
        if (AccountStatusUtils.isDisabled(user)) {
            throw new AccountDisabledException();
        }
    }

    private ClientApplication requireActiveApplication(String appId) {
        ClientApplication clientApplication;
        try {
            clientApplication = this.clientApplicationRepository.findClientApplicationById(appId);
        } catch (ClientApplicationNotFoundException e) {
            throw new ApplicationAuthenticationFailedException();
        }
        if (Boolean.FALSE.equals(clientApplication.getActive())) {
            throw new ApplicationAuthenticationFailedException();
        }
        return clientApplication;
    }

    private void ensureLoginNotThrottled(String action, String identifier) {
        this.ensureIpNotThrottled("user-login-ip");
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
        Duration window = Duration.ofSeconds(resolveLimit(this.loginWindowSeconds, 900));
        this.requestThrottleService.recordAttempt(action, identifier, window);
        this.requestThrottleService.recordAttempt(
                "user-login-ip",
                this.clientRequestContext.clientIp(),
                Duration.ofSeconds(resolveLimit(this.loginIpWindowSeconds, 900))
        );
    }

    private int resolveLimit(Integer configuredValue, int fallback) {
        return configuredValue != null && configuredValue > 0 ? configuredValue : fallback;
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

        return this.resolveDefaultAccessMinutes();
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

    private long resolveDefaultAccessMinutes() {
        if (this.tokenExpirationMinutes != null && this.tokenExpirationMinutes > 0) {
            return this.tokenExpirationMinutes;
        }
        return 15L;
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
}
