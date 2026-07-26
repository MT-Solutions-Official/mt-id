package com.mtsolutions.domain.service;

import com.mtsolutions.application.exception.EmailAlreadyVerifiedException;
import com.mtsolutions.application.exception.InvalidOrExpiredTokenException;
import com.mtsolutions.application.exception.TooManyRequestsException;
import com.mtsolutions.application.exception.UserNotFoundException;
import com.mtsolutions.application.model.EmailBranding;
import com.mtsolutions.application.service.EmailService;
import com.mtsolutions.application.utils.DateUtils;
import com.mtsolutions.domain.entity.ClientApplication;
import com.mtsolutions.domain.entity.Owner;
import com.mtsolutions.domain.entity.User;
import com.mtsolutions.domain.model.Email;
import com.mtsolutions.domain.model.Password;
import com.mtsolutions.domain.repository.ClientApplicationRepository;
import com.mtsolutions.domain.repository.OwnerRepository;
import com.mtsolutions.domain.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@Slf4j
public class AccountEmailService {

    @ConfigProperty(name = "app.mt.id.base-url")
    String baseUrl;

    @ConfigProperty(name = "app.mt.id.email.verification.token.ttl.minutes")
    Integer verificationTokenTtlMinutes;

    @ConfigProperty(name = "app.mt.id.email.password.reset.token.ttl.minutes")
    Integer passwordResetTokenTtlMinutes;

    @ConfigProperty(name = "app.mt.id.throttle.verification-send.max-attempts")
    Integer verificationSendMaxAttempts;

    @ConfigProperty(name = "app.mt.id.throttle.verification-send.window.seconds")
    Integer verificationSendWindowSeconds;

    @ConfigProperty(name = "app.mt.id.throttle.verification-send.min-interval.seconds")
    Integer verificationSendMinIntervalSeconds;

    @ConfigProperty(name = "app.mt.id.throttle.forgot-password.max-attempts")
    Integer forgotPasswordMaxAttempts;

    @ConfigProperty(name = "app.mt.id.throttle.forgot-password.window.seconds")
    Integer forgotPasswordWindowSeconds;

    @ConfigProperty(name = "app.mt.id.throttle.forgot-password.min-interval.seconds")
    Integer forgotPasswordMinIntervalSeconds;

    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final ClientApplicationRepository clientApplicationRepository;
    private final EmailService emailService;
    private final DateUtils dateUtils;
    private final BcryptService bcryptService;
    private final RequestThrottleService requestThrottleService;

    public AccountEmailService(UserRepository userRepository,
                               OwnerRepository ownerRepository,
                               ClientApplicationRepository clientApplicationRepository,
                               EmailService emailService,
                               DateUtils dateUtils,
                               BcryptService bcryptService,
                               RequestThrottleService requestThrottleService) {
        this.userRepository = userRepository;
        this.ownerRepository = ownerRepository;
        this.clientApplicationRepository = clientApplicationRepository;
        this.emailService = emailService;
        this.dateUtils = dateUtils;
        this.bcryptService = bcryptService;
        this.requestThrottleService = requestThrottleService;
    }

    public void sendUserVerificationEmail(String userId, String emailAddress) {
        String throttleKey = userId + ":" + emailAddress;
        if (this.requestThrottleService.shouldThrottle(
                "user-email-verification-send",
                throttleKey,
                resolveLimit(this.verificationSendMaxAttempts, 5),
                Duration.ofSeconds(resolveLimit(this.verificationSendWindowSeconds, 3600)),
                Duration.ofSeconds(resolveLimit(this.verificationSendMinIntervalSeconds, 30)))) {
            throw new TooManyRequestsException();
        }

        User user = this.userRepository.findUserById(userId);
        Email targetEmail = resolveUserEmail(user, emailAddress);
        if (Boolean.TRUE.equals(targetEmail.getVerified())) {
            throw new EmailAlreadyVerifiedException();
        }

        ClientApplication clientApplication = this.clientApplicationRepository.findClientApplicationById(user.getAppId());
        String token = UUID.randomUUID().toString();
        targetEmail.setVerificationToken(token);
        targetEmail.setVerificationTokenExpiry(this.dateUtils.now().plusMinutes(resolveLimit(this.verificationTokenTtlMinutes, 30)));
        user.setUpdatedAt(this.dateUtils.now());
        this.userRepository.persistOrUpdate(user);

        this.emailService.sendVerificationEmail(
                EmailBranding.fromClientApplication(clientApplication),
                targetEmail.getEmail(),
                resolveDisplayName(user.getName(), targetEmail.getEmail()),
                resolveEmailVerificationUrl(clientApplication, token)
        );
    }

    public void verifyUserEmail(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidOrExpiredTokenException();
        }

        User user = this.userRepository.findUserByEmailVerificationToken(token)
                .orElseThrow(InvalidOrExpiredTokenException::new);
        Email targetEmail = findUserEmailByVerificationToken(user, token);
        verifyEmail(targetEmail);
        user.setUpdatedAt(this.dateUtils.now());
        this.userRepository.persistOrUpdate(user);
    }

    public void sendUserPasswordResetEmail(String emailAddress, String appId) {
        String throttleKey = emailAddress + ":" + appId;
        if (this.requestThrottleService.shouldThrottle(
                "user-forgot-password",
                throttleKey,
                resolveLimit(this.forgotPasswordMaxAttempts, 5),
                Duration.ofSeconds(resolveLimit(this.forgotPasswordWindowSeconds, 3600)),
                Duration.ofSeconds(resolveLimit(this.forgotPasswordMinIntervalSeconds, 30)))) {
            return;
        }

        User user;
        try {
            user = this.userRepository.findUserByEmail(emailAddress);
        } catch (UserNotFoundException e) {
            log.debug("Ignoring password reset for unknown user email: {}", emailAddress);
            return;
        }

        if (appId != null && !appId.isBlank() && !appId.equals(user.getAppId())) {
            return;
        }

        ClientApplication clientApplication = this.clientApplicationRepository.findClientApplicationById(user.getAppId());
        Password password = user.getPassword() != null ? user.getPassword() : Password.builder().build();
        String token = UUID.randomUUID().toString();
        // Reissuing a reset email rotates token and invalidates previous links.
        password.setPasswordResetToken(token);
        password.setPasswordResetTokenExpiry(this.dateUtils.now().plusMinutes(resolveLimit(this.passwordResetTokenTtlMinutes, 15)));
        user.setPassword(password);
        user.setUpdatedAt(this.dateUtils.now());
        this.userRepository.persistOrUpdate(user);

        String primaryEmail = resolvePrimaryUserEmail(user);
        this.emailService.sendPasswordResetEmail(
                EmailBranding.fromClientApplication(clientApplication),
                primaryEmail,
                resolveDisplayName(user.getName(), primaryEmail),
                resolvePasswordResetUrl(clientApplication, token)
        );
    }

    public void resetUserPassword(String token, String newPassword) {
        if (token == null || token.isBlank() || newPassword == null || newPassword.isBlank()) {
            throw new InvalidOrExpiredTokenException();
        }

        User user = this.userRepository.findUserByPasswordResetToken(token)
                .orElseThrow(InvalidOrExpiredTokenException::new);
        Password password = user.getPassword();
        if (password == null || !token.equals(password.getPasswordResetToken())) {
            throw new InvalidOrExpiredTokenException();
        }
        ensureTokenNotExpired(password.getPasswordResetTokenExpiry());

        password.setPassword(this.bcryptService.encryptPassword(newPassword));
        // Single-use token: invalidate immediately after successful reset.
        password.setPasswordResetToken(null);
        password.setPasswordResetTokenExpiry(null);
        user.setUpdatedAt(this.dateUtils.now());
        this.userRepository.persistOrUpdate(user);

        String primaryEmail = resolvePrimaryUserEmail(user);
        notifyPasswordChanged(user.getAppId(), primaryEmail, resolveDisplayName(user.getName(), primaryEmail));
    }

    public void sendOwnerVerificationEmail(String ownerId, String emailAddress) {
        String throttleKey = ownerId + ":" + emailAddress;
        if (this.requestThrottleService.shouldThrottle(
                "owner-email-verification-send",
                throttleKey,
                resolveLimit(this.verificationSendMaxAttempts, 5),
                Duration.ofSeconds(resolveLimit(this.verificationSendWindowSeconds, 3600)),
                Duration.ofSeconds(resolveLimit(this.verificationSendMinIntervalSeconds, 30)))) {
            throw new TooManyRequestsException();
        }

        Owner owner = this.ownerRepository.findOwnerById(ownerId);
        if (owner.getEmail() == null) {
            throw new InvalidOrExpiredTokenException();
        }
        if (emailAddress != null && !emailAddress.isBlank() && !emailAddress.equalsIgnoreCase(owner.getEmail().getEmail())) {
            throw new InvalidOrExpiredTokenException();
        }
        if (Boolean.TRUE.equals(owner.getEmail().getVerified())) {
            throw new EmailAlreadyVerifiedException();
        }

        String token = UUID.randomUUID().toString();
        owner.getEmail().setVerificationToken(token);
        owner.getEmail().setVerificationTokenExpiry(this.dateUtils.now().plusMinutes(resolveLimit(this.verificationTokenTtlMinutes, 30)));
        owner.setUpdatedAt(this.dateUtils.now());
        this.ownerRepository.persistOrUpdate(owner);

        this.emailService.sendVerificationEmail(
                platformBranding(),
                owner.getEmail().getEmail(),
                resolveDisplayName(owner.getName(), owner.getEmail().getEmail()),
                resolvePlatformVerificationUrl(token)
        );
    }

    public void verifyOwnerEmail(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidOrExpiredTokenException();
        }

        Owner owner = this.ownerRepository.findOwnerByEmailVerificationToken(token)
                .orElseThrow(InvalidOrExpiredTokenException::new);
        if (owner.getEmail() == null || !token.equals(owner.getEmail().getVerificationToken())) {
            throw new InvalidOrExpiredTokenException();
        }
        verifyEmail(owner.getEmail());
        owner.setUpdatedAt(this.dateUtils.now());
        this.ownerRepository.persistOrUpdate(owner);
    }

    public void sendOwnerPasswordResetEmail(String emailAddress) {
        if (this.requestThrottleService.shouldThrottle(
                "owner-forgot-password",
                emailAddress,
                resolveLimit(this.forgotPasswordMaxAttempts, 5),
                Duration.ofSeconds(resolveLimit(this.forgotPasswordWindowSeconds, 3600)),
                Duration.ofSeconds(resolveLimit(this.forgotPasswordMinIntervalSeconds, 30)))) {
            return;
        }

        Optional<Owner> maybeOwner = this.ownerRepository.findOwnerByEmail(emailAddress);
        if (maybeOwner.isEmpty()) {
            return;
        }

        Owner owner = maybeOwner.get();
        Password password = owner.getPassword() != null ? owner.getPassword() : Password.builder().build();
        String token = UUID.randomUUID().toString();
        // Reissuing a reset email rotates token and invalidates previous links.
        password.setPasswordResetToken(token);
        password.setPasswordResetTokenExpiry(this.dateUtils.now().plusMinutes(resolveLimit(this.passwordResetTokenTtlMinutes, 15)));
        owner.setPassword(password);
        owner.setUpdatedAt(this.dateUtils.now());
        this.ownerRepository.persistOrUpdate(owner);

        this.emailService.sendPasswordResetEmail(
                platformBranding(),
                owner.getEmail().getEmail(),
                resolveDisplayName(owner.getName(), owner.getEmail().getEmail()),
                resolvePlatformPasswordResetUrl(token)
        );
    }

    public void resetOwnerPassword(String token, String newPassword) {
        if (token == null || token.isBlank() || newPassword == null || newPassword.isBlank()) {
            throw new InvalidOrExpiredTokenException();
        }

        Owner owner = this.ownerRepository.findOwnerByPasswordResetToken(token)
                .orElseThrow(InvalidOrExpiredTokenException::new);
        Password password = owner.getPassword();
        if (password == null || !token.equals(password.getPasswordResetToken())) {
            throw new InvalidOrExpiredTokenException();
        }
        ensureTokenNotExpired(password.getPasswordResetTokenExpiry());

        password.setPassword(this.bcryptService.encryptPassword(newPassword));
        // Single-use token: invalidate immediately after successful reset.
        password.setPasswordResetToken(null);
        password.setPasswordResetTokenExpiry(null);
        owner.setUpdatedAt(this.dateUtils.now());
        this.ownerRepository.persistOrUpdate(owner);

        notifyPasswordChanged(null, owner.getEmail() != null ? owner.getEmail().getEmail() : null, resolveDisplayName(owner.getName(), owner.getEmail() != null ? owner.getEmail().getEmail() : null));
    }

    private void verifyEmail(Email email) {
        ensureTokenNotExpired(email.getVerificationTokenExpiry());
        // Single-use token: invalidate immediately after successful verification.
        email.setVerified(true);
        email.setVerificationToken(null);
        email.setVerificationTokenExpiry(null);
    }

    private Email resolveUserEmail(User user, String requestedEmail) {
        if (user.getEmails() == null || user.getEmails().isEmpty()) {
            throw new InvalidOrExpiredTokenException();
        }

        if (requestedEmail != null && !requestedEmail.isBlank()) {
            return user.getEmails().stream()
                    .filter(email -> email != null && requestedEmail.equalsIgnoreCase(email.getEmail()))
                    .findFirst()
                    .orElseThrow(InvalidOrExpiredTokenException::new);
        }

        return user.getEmails().stream()
                .filter(email -> email != null && Boolean.TRUE.equals(email.getPrimary()))
                .findFirst()
                .orElseGet(() -> user.getEmails().stream()
                        .filter(email -> email != null && email.getEmail() != null)
                        .findFirst()
                        .orElseThrow(InvalidOrExpiredTokenException::new));
    }

    private Email findUserEmailByVerificationToken(User user, String token) {
        return user.getEmails().stream()
                .filter(email -> email != null && token.equals(email.getVerificationToken()))
                .findFirst()
                .orElseThrow(InvalidOrExpiredTokenException::new);
    }

    private String resolvePrimaryUserEmail(User user) {
        if (user.getEmails() == null || user.getEmails().isEmpty()) {
            throw new InvalidOrExpiredTokenException();
        }

        return user.getEmails().stream()
                .filter(email -> email != null && Boolean.TRUE.equals(email.getPrimary()) && email.getEmail() != null)
                .map(Email::getEmail)
                .findFirst()
                .orElseGet(() -> user.getEmails().stream()
                        .filter(email -> email != null && email.getEmail() != null)
                        .map(Email::getEmail)
                        .findFirst()
                        .orElseThrow(InvalidOrExpiredTokenException::new));
    }

    private EmailBranding platformBranding() {
        return EmailBranding.of("MT ID", null, null, null, null, null, baseUrl);
    }

    private String resolveEmailVerificationUrl(ClientApplication clientApplication, String token) {
        if (clientApplication.getEmailSettings() != null
                && clientApplication.getEmailSettings().getVerificationRedirectUrl() != null
                && !clientApplication.getEmailSettings().getVerificationRedirectUrl().isBlank()) {
            return clientApplication.getEmailSettings().getVerificationRedirectUrl() + "?token=" + token;
        }
        return baseUrl + "/api/v1/users/email/verify?token=" + token;
    }

    private String resolvePasswordResetUrl(ClientApplication clientApplication, String token) {
        if (clientApplication.getEmailSettings() != null
                && clientApplication.getEmailSettings().getPasswordResetRedirectUrl() != null
                && !clientApplication.getEmailSettings().getPasswordResetRedirectUrl().isBlank()) {
            return clientApplication.getEmailSettings().getPasswordResetRedirectUrl() + "?token=" + token;
        }
        return baseUrl + "/api/v1/users/password/reset?token=" + token;
    }

    private String resolvePlatformVerificationUrl(String token) {
        return baseUrl + "/api/v1/owner/email/verify?token=" + token;
    }

    private String resolvePlatformPasswordResetUrl(String token) {
        return baseUrl + "/api/v1/owner/password/reset?token=" + token;
    }

    private void notifyPasswordChanged(String appId, String email, String displayName) {
        EmailBranding branding = appId != null
                ? EmailBranding.fromClientApplication(this.clientApplicationRepository.findClientApplicationById(appId))
                : platformBranding();

        this.emailService.sendPasswordChangedEmail(
                branding,
                email,
                displayName,
                this.dateUtils.now().toString(),
                baseUrl
        );
    }

    private void ensureTokenNotExpired(LocalDateTime expiry) {
        if (expiry == null || this.dateUtils.now().isAfter(expiry)) {
            throw new InvalidOrExpiredTokenException();
        }
    }

    private String resolveDisplayName(String name, String email) {
        if (name != null && !name.isBlank()) {
            return name;
        }

        return email;
    }

    private int resolveLimit(Integer configuredValue, int fallback) {
        return configuredValue != null && configuredValue > 0 ? configuredValue : fallback;
    }
}

