package com.mtsolutions.domain.service;

import com.mtsolutions.application.common.ContextComponent;
import com.mtsolutions.application.exception.ApplicationForbiddenException;
import com.mtsolutions.application.exception.EmailAlreadyVerifiedException;
import com.mtsolutions.application.exception.InvalidOrExpiredTokenException;
import com.mtsolutions.application.exception.TooManyRequestsException;
import com.mtsolutions.application.model.EmailBranding;
import com.mtsolutions.application.service.EmailLinkService;
import com.mtsolutions.application.service.EmailService;
import com.mtsolutions.application.utils.AccountStatusUtils;
import com.mtsolutions.application.utils.DateUtils;
import com.mtsolutions.application.utils.NormalizeUtils;
import com.mtsolutions.domain.entity.ClientApplication;
import com.mtsolutions.domain.entity.Owner;
import com.mtsolutions.domain.entity.User;
import com.mtsolutions.domain.model.AppOwnerAccess;
import com.mtsolutions.domain.model.Email;
import com.mtsolutions.domain.model.Password;
import com.mtsolutions.domain.repository.ClientApplicationRepository;
import com.mtsolutions.domain.repository.OwnerRepository;
import com.mtsolutions.domain.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@Slf4j
public class AccountEmailService {

    @ConfigProperty(name = "app.mt.id.frontend.base-url")
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
    private final PasswordPolicyService passwordPolicyService;
    private final TokenHashService tokenHashService;
    private final RequestThrottleService requestThrottleService;
    private final ContextComponent contextComponent;
    private final EmailLinkService emailLinkService;

    public AccountEmailService(UserRepository userRepository,
                               OwnerRepository ownerRepository,
                               ClientApplicationRepository clientApplicationRepository,
                               EmailService emailService,
                               DateUtils dateUtils,
                               BcryptService bcryptService,
                               PasswordPolicyService passwordPolicyService,
                               TokenHashService tokenHashService,
                               RequestThrottleService requestThrottleService,
                               ContextComponent contextComponent,
                               EmailLinkService emailLinkService) {
        this.userRepository = userRepository;
        this.ownerRepository = ownerRepository;
        this.clientApplicationRepository = clientApplicationRepository;
        this.emailService = emailService;
        this.dateUtils = dateUtils;
        this.bcryptService = bcryptService;
        this.passwordPolicyService = passwordPolicyService;
        this.tokenHashService = tokenHashService;
        this.requestThrottleService = requestThrottleService;
        this.contextComponent = contextComponent;
        this.emailLinkService = emailLinkService;
    }

    public void sendUserVerificationEmailAfterCreate(User user) {
        if (user == null || user.getUserId() == null || user.getPrimaryEmail() == null) {
            return;
        }
        try {
            this.sendUserVerificationEmail(user.getUserId(), user.getPrimaryEmail(), false, true);
        } catch (RuntimeException e) {
            log.error("Failed to send verification email after user create", e);
        }
    }

    public void notifyUserPasswordChanged(User user) {
        if (user == null) {
            return;
        }
        try {
            String primaryEmail = resolvePrimaryUserEmail(user);
            notifyPasswordChanged(user.getAppId(), primaryEmail, resolveDisplayName(user.getName(), primaryEmail));
        } catch (RuntimeException e) {
            log.error("Failed to send password-changed email after user profile update", e);
        }
    }

    public void sendUserVerificationEmail(String userId, String emailAddress) {
        this.sendUserVerificationEmail(userId, emailAddress, true, false);
    }

    private void sendUserVerificationEmail(String userId, String emailAddress, boolean enforceAccess, boolean bestEffort) {
        User user = this.userRepository.findUserById(userId);
        if (enforceAccess) {
            this.contextComponent.validateAppOrSelfAccess(user.getAppId(), user.getUserId());
        }

        if (!bestEffort) {
            String throttleKey = userId + ":" + emailAddress;
            if (this.requestThrottleService.shouldThrottle(
                    "user-email-verification-send",
                    throttleKey,
                    resolveLimit(this.verificationSendMaxAttempts, 5),
                    Duration.ofSeconds(resolveLimit(this.verificationSendWindowSeconds, 3600)),
                    Duration.ofSeconds(resolveLimit(this.verificationSendMinIntervalSeconds, 30)))) {
                throw new TooManyRequestsException();
            }
        }

        Email targetEmail = resolveUserEmail(user, emailAddress);
        if (Boolean.TRUE.equals(targetEmail.getVerified())) {
            if (bestEffort) {
                return;
            }
            throw new EmailAlreadyVerifiedException();
        }

        ClientApplication clientApplication = this.clientApplicationRepository.findClientApplicationById(user.getAppId());
        String rawToken = this.newRawToken();
        targetEmail.setVerificationToken(this.tokenHashService.hash(rawToken));
        targetEmail.setVerificationTokenExpiry(this.dateUtils.now().plusMinutes(resolveLimit(this.verificationTokenTtlMinutes, 30)));
        user.setUpdatedAt(this.dateUtils.now());
        this.userRepository.persistOrUpdate(user);

        this.dispatchEmail(bestEffort, () -> this.emailService.sendVerificationEmail(
                EmailBranding.fromClientApplication(clientApplication),
                targetEmail.getEmail(),
                resolveDisplayName(user.getName(), targetEmail.getEmail()),
                this.emailLinkService.userVerificationUrl(clientApplication, rawToken)
        ));
    }

    public void verifyUserEmail(String token) {
        String tokenHash = this.requireTokenHash(token);
        User user = this.userRepository.findUserByEmailVerificationToken(tokenHash)
                .orElseThrow(InvalidOrExpiredTokenException::new);
        Email targetEmail = findUserEmailByVerificationToken(user, tokenHash);
        verifyEmail(targetEmail);
        user.setUpdatedAt(this.dateUtils.now());
        this.userRepository.persistOrUpdate(user);
    }

    public void sendUserPasswordResetEmail(String emailAddress, String appId) {
        String normalizedEmail = NormalizeUtils.normalizeEmail(emailAddress);
        String normalizedAppId = NormalizeUtils.trimToNull(appId);
        if (normalizedEmail == null || normalizedAppId == null) {
            throw new BadRequestException("Email and appId are required.");
        }

        String throttleKey = normalizedEmail + ":" + normalizedAppId;
        if (this.requestThrottleService.shouldThrottle(
                "user-forgot-password",
                throttleKey,
                resolveLimit(this.forgotPasswordMaxAttempts, 5),
                Duration.ofSeconds(resolveLimit(this.forgotPasswordWindowSeconds, 3600)),
                Duration.ofSeconds(resolveLimit(this.forgotPasswordMinIntervalSeconds, 30)))) {
            return;
        }

        Optional<User> maybeUser = this.userRepository.findUserByAppIdAndEmail(normalizedAppId, normalizedEmail);
        if (maybeUser.isEmpty()) {
            log.debug("Ignoring password reset for unknown user email in app {}", normalizedAppId);
            return;
        }

        User user = maybeUser.get();
        if (AccountStatusUtils.isDisabled(user)) {
            log.debug("Ignoring password reset for disabled user in app {}", normalizedAppId);
            return;
        }

        ClientApplication clientApplication = this.clientApplicationRepository.findClientApplicationById(user.getAppId());
        Password password = user.getPassword() != null ? user.getPassword() : Password.builder().build();
        String rawToken = this.newRawToken();
        // Reissuing a reset email rotates token and invalidates previous links.
        password.setPasswordResetToken(this.tokenHashService.hash(rawToken));
        password.setPasswordResetTokenExpiry(this.dateUtils.now().plusMinutes(resolveLimit(this.passwordResetTokenTtlMinutes, 15)));
        user.setPassword(password);
        user.setUpdatedAt(this.dateUtils.now());
        this.userRepository.persistOrUpdate(user);

        this.dispatchEmail(true, () -> this.emailService.sendPasswordResetEmail(
                EmailBranding.fromClientApplication(clientApplication),
                normalizedEmail,
                resolveDisplayName(user.getName(), normalizedEmail),
                this.emailLinkService.userPasswordResetUrl(clientApplication, rawToken)
        ));
    }

    public void resetUserPassword(String token, String newPassword) {
        String tokenHash = this.requireTokenHash(token);
        if (newPassword == null || newPassword.isBlank()) {
            throw new InvalidOrExpiredTokenException();
        }

        User user = this.userRepository.findUserByPasswordResetToken(tokenHash)
                .orElseThrow(InvalidOrExpiredTokenException::new);
        if (AccountStatusUtils.isDisabled(user)) {
            throw new InvalidOrExpiredTokenException();
        }
        Password password = user.getPassword();
        if (password == null || !tokenHash.equals(password.getPasswordResetToken())) {
            throw new InvalidOrExpiredTokenException();
        }
        ensureTokenNotExpired(password.getPasswordResetTokenExpiry());
        this.passwordPolicyService.validate(newPassword);

        password.setPassword(this.bcryptService.encryptPassword(newPassword));
        // Single-use token: invalidate immediately after successful reset.
        password.setPasswordResetToken(null);
        password.setPasswordResetTokenExpiry(null);
        user.setUpdatedAt(this.dateUtils.now());
        this.userRepository.persistOrUpdate(user);

        String primaryEmail = resolvePrimaryUserEmail(user);
        notifyPasswordChanged(user.getAppId(), primaryEmail, resolveDisplayName(user.getName(), primaryEmail));
    }

    public void sendOwnerVerificationEmailAfterCreate(Owner owner) {
        if (owner == null || owner.getOwnerId() == null || owner.getEmail() == null) {
            return;
        }
        try {
            this.sendOwnerVerificationEmail(owner.getOwnerId(), owner.getEmail().getEmail(), false, true);
        } catch (RuntimeException e) {
            log.error("Failed to send verification email after owner create", e);
        }
    }

    public void sendOwnerVerificationEmail(String ownerId, String emailAddress) {
        this.sendOwnerVerificationEmail(ownerId, emailAddress, true, false);
    }

    private void sendOwnerVerificationEmail(String ownerId, String emailAddress, boolean enforceAccess, boolean bestEffort) {
        if (enforceAccess) {
            this.validateOwnerEmailAccess(ownerId);
        }

        if (!bestEffort) {
            String throttleKey = ownerId + ":" + emailAddress;
            if (this.requestThrottleService.shouldThrottle(
                    "owner-email-verification-send",
                    throttleKey,
                    resolveLimit(this.verificationSendMaxAttempts, 5),
                    Duration.ofSeconds(resolveLimit(this.verificationSendWindowSeconds, 3600)),
                    Duration.ofSeconds(resolveLimit(this.verificationSendMinIntervalSeconds, 30)))) {
                throw new TooManyRequestsException();
            }
        }

        Owner owner = this.ownerRepository.findOwnerById(ownerId);
        if (owner.getEmail() == null) {
            if (bestEffort) {
                return;
            }
            throw new InvalidOrExpiredTokenException();
        }
        if (emailAddress != null && !emailAddress.isBlank() && !emailAddress.equalsIgnoreCase(owner.getEmail().getEmail())) {
            if (bestEffort) {
                return;
            }
            throw new InvalidOrExpiredTokenException();
        }
        if (Boolean.TRUE.equals(owner.getEmail().getVerified())) {
            if (bestEffort) {
                return;
            }
            throw new EmailAlreadyVerifiedException();
        }

        String rawToken = this.newRawToken();
        owner.getEmail().setVerificationToken(this.tokenHashService.hash(rawToken));
        owner.getEmail().setVerificationTokenExpiry(this.dateUtils.now().plusMinutes(resolveLimit(this.verificationTokenTtlMinutes, 30)));
        owner.setUpdatedAt(this.dateUtils.now());
        this.ownerRepository.persistOrUpdate(owner);

        this.dispatchEmail(bestEffort, () -> this.emailService.sendVerificationEmail(
                platformBranding(),
                owner.getEmail().getEmail(),
                resolveDisplayName(owner.getName(), owner.getEmail().getEmail()),
                this.emailLinkService.ownerVerificationUrl(rawToken)
        ));
    }

    public void verifyOwnerEmail(String token) {
        String tokenHash = this.requireTokenHash(token);
        Owner owner = this.ownerRepository.findOwnerByEmailVerificationToken(tokenHash)
                .orElseThrow(InvalidOrExpiredTokenException::new);
        if (owner.getEmail() == null || !tokenHash.equals(owner.getEmail().getVerificationToken())) {
            throw new InvalidOrExpiredTokenException();
        }
        verifyEmail(owner.getEmail());
        owner.setUpdatedAt(this.dateUtils.now());
        this.ownerRepository.persistOrUpdate(owner);
    }

    public void sendOwnerPasswordResetEmail(String emailAddress) {
        String normalizedEmail = NormalizeUtils.normalizeEmail(emailAddress);
        if (normalizedEmail == null) {
            return;
        }

        if (this.requestThrottleService.shouldThrottle(
                "owner-forgot-password",
                normalizedEmail,
                resolveLimit(this.forgotPasswordMaxAttempts, 5),
                Duration.ofSeconds(resolveLimit(this.forgotPasswordWindowSeconds, 3600)),
                Duration.ofSeconds(resolveLimit(this.forgotPasswordMinIntervalSeconds, 30)))) {
            return;
        }

        Optional<Owner> maybeOwner = this.ownerRepository.findOwnerByEmail(normalizedEmail);
        if (maybeOwner.isEmpty()) {
            return;
        }

        Owner owner = maybeOwner.get();
        if (AccountStatusUtils.isDisabled(owner)) {
            return;
        }
        Password password = owner.getPassword() != null ? owner.getPassword() : Password.builder().build();
        String rawToken = this.newRawToken();
        // Reissuing a reset email rotates token and invalidates previous links.
        password.setPasswordResetToken(this.tokenHashService.hash(rawToken));
        password.setPasswordResetTokenExpiry(this.dateUtils.now().plusMinutes(resolveLimit(this.passwordResetTokenTtlMinutes, 15)));
        owner.setPassword(password);
        owner.setUpdatedAt(this.dateUtils.now());
        this.ownerRepository.persistOrUpdate(owner);

        this.dispatchEmail(true, () -> this.emailService.sendPasswordResetEmail(
                platformBranding(),
                owner.getEmail().getEmail(),
                resolveDisplayName(owner.getName(), owner.getEmail().getEmail()),
                this.emailLinkService.ownerPasswordResetUrl(rawToken)
        ));
    }

    public void resetOwnerPassword(String token, String newPassword) {
        String tokenHash = this.requireTokenHash(token);
        if (newPassword == null || newPassword.isBlank()) {
            throw new InvalidOrExpiredTokenException();
        }

        Owner owner = this.ownerRepository.findOwnerByPasswordResetToken(tokenHash)
                .orElseThrow(InvalidOrExpiredTokenException::new);
        if (AccountStatusUtils.isDisabled(owner)) {
            throw new InvalidOrExpiredTokenException();
        }
        Password password = owner.getPassword();
        if (password == null || !tokenHash.equals(password.getPasswordResetToken())) {
            throw new InvalidOrExpiredTokenException();
        }
        ensureTokenNotExpired(password.getPasswordResetTokenExpiry());
        this.passwordPolicyService.validate(newPassword);

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
            String normalizedRequestedEmail = NormalizeUtils.normalizeEmail(requestedEmail);
            return user.getEmails().stream()
                    .filter(email -> email != null && normalizedRequestedEmail != null
                            && normalizedRequestedEmail.equalsIgnoreCase(email.getEmail()))
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
        return EmailBranding.of("MT ID", null, null, "MT ID", null, null, this.baseUrl);
    }

    private void notifyPasswordChanged(String appId, String email, String displayName) {
        if (email == null || email.isBlank()) {
            return;
        }

        EmailBranding branding;
        String accountUrl;
        if (appId != null) {
            ClientApplication clientApplication = this.clientApplicationRepository.findClientApplicationById(appId);
            branding = EmailBranding.fromClientApplication(clientApplication);
            accountUrl = this.emailLinkService.userAccountUrl(clientApplication);
        } else {
            branding = platformBranding();
            accountUrl = this.emailLinkService.ownerAccountUrl();
        }

        this.dispatchEmail(true, () -> this.emailService.sendPasswordChangedEmail(
                branding,
                email,
                displayName,
                this.dateUtils.formatDisplay(this.dateUtils.now()),
                accountUrl
        ));
    }

    private void validateOwnerEmailAccess(String ownerId) {
        String authenticatedOwnerId = this.contextComponent.getAuthenticatedOwnerId();
        if (authenticatedOwnerId != null && authenticatedOwnerId.equals(ownerId)) {
            return;
        }
        if (!this.contextComponent.isOwnerActor()) {
            throw new ApplicationForbiddenException();
        }
        boolean writerOnSharedApp = this.clientApplicationRepository.findByOwnerId(authenticatedOwnerId).stream()
                .anyMatch(application -> AppOwnerAccess.isWriter(application, authenticatedOwnerId)
                        && AppOwnerAccess.isMember(application, ownerId));
        if (!writerOnSharedApp) {
            throw new ApplicationForbiddenException();
        }
    }

    private void dispatchEmail(boolean bestEffort, Runnable sender) {
        try {
            sender.run();
        } catch (RuntimeException e) {
            if (!bestEffort) {
                throw e;
            }
            log.error("Failed to dispatch account email", e);
        }
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

    private String newRawToken() {
        return UUID.randomUUID().toString();
    }

    private String requireTokenHash(String rawToken) {
        String hashed = this.tokenHashService.hash(rawToken);
        if (hashed == null) {
            throw new InvalidOrExpiredTokenException();
        }
        return hashed;
    }

    private int resolveLimit(Integer configuredValue, int fallback) {
        return configuredValue != null && configuredValue > 0 ? configuredValue : fallback;
    }
}

