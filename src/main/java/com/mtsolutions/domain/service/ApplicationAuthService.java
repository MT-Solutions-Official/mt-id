package com.mtsolutions.domain.service;

import com.mtsolutions.application.common.ClientRequestContext;
import com.mtsolutions.application.exception.ApplicationAuthenticationFailedException;
import com.mtsolutions.application.exception.TooManyRequestsException;
import com.mtsolutions.domain.dto.response.AppTokenResponseDto;
import com.mtsolutions.domain.entity.ClientApplication;
import com.mtsolutions.domain.repository.ClientApplicationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;

@ApplicationScoped
public class ApplicationAuthService {

    @ConfigProperty(name = "app.mt.id.token.expiration.minutes")
    Integer tokenExpirationMinutes;

    @ConfigProperty(name = "app.mt.id.throttle.application-token.max-attempts")
    Integer tokenMaxAttempts;

    @ConfigProperty(name = "app.mt.id.throttle.application-token.window.seconds")
    Integer tokenWindowSeconds;

    @ConfigProperty(name = "app.mt.id.throttle.application-token.min-interval.seconds")
    Integer tokenMinIntervalSeconds;

    @ConfigProperty(name = "app.mt.id.throttle.application-token.ip.max-attempts")
    Integer tokenIpMaxAttempts;

    @ConfigProperty(name = "app.mt.id.throttle.application-token.ip.window.seconds")
    Integer tokenIpWindowSeconds;

    @ConfigProperty(name = "app.mt.id.throttle.application-token.ip.min-interval.seconds")
    Integer tokenIpMinIntervalSeconds;

    private final ClientApplicationRepository clientApplicationRepository;
    private final BcryptService bcryptService;
    private final JwtService jwtService;
    private final RequestThrottleService requestThrottleService;
    private final ClientRequestContext clientRequestContext;

    public ApplicationAuthService(ClientApplicationRepository clientApplicationRepository,
                                  BcryptService bcryptService,
                                  JwtService jwtService,
                                  RequestThrottleService requestThrottleService,
                                  ClientRequestContext clientRequestContext) {
        this.clientApplicationRepository = clientApplicationRepository;
        this.bcryptService = bcryptService;
        this.jwtService = jwtService;
        this.requestThrottleService = requestThrottleService;
        this.clientRequestContext = clientRequestContext;
    }

    public AppTokenResponseDto generateApplicationToken(String apiKey, String apiSecret) {
        String normalizedApiKey = apiKey != null ? apiKey.trim() : null;
        String throttleId = normalizedApiKey != null && !normalizedApiKey.isBlank() ? normalizedApiKey : "unknown";
        this.ensureNotThrottled(throttleId);

        try {
            if (normalizedApiKey == null || normalizedApiKey.isBlank()
                    || apiSecret == null || apiSecret.isBlank()) {
                throw new ApplicationAuthenticationFailedException();
            }

            ClientApplication clientApplication = this.clientApplicationRepository.findClientApplicationByApiKey(normalizedApiKey)
                    .orElseThrow(ApplicationAuthenticationFailedException::new);

            if (Boolean.FALSE.equals(clientApplication.getActive())
                    || clientApplication.getApiSecret() == null
                    || !this.bcryptService.verifyPassword(apiSecret, clientApplication.getApiSecret())) {
                throw new ApplicationAuthenticationFailedException();
            }

            this.requestThrottleService.clear("application-token", throttleId);
            Duration expiration = Duration.ofMinutes(resolveApplicationJwtExpiration(clientApplication));
            return this.jwtService.generateApplicationToken(clientApplication, expiration);
        } catch (ApplicationAuthenticationFailedException e) {
            this.recordFailure(throttleId);
            throw e;
        }
    }

    private void ensureNotThrottled(String apiKey) {
        Duration window = Duration.ofSeconds(resolveLimit(this.tokenWindowSeconds, 900));
        Duration minInterval = Duration.ofSeconds(resolveLimit(this.tokenMinIntervalSeconds, 1));
        Duration ipWindow = Duration.ofSeconds(resolveLimit(this.tokenIpWindowSeconds, 900));
        Duration ipMinInterval = Duration.ofSeconds(resolveLimit(this.tokenIpMinIntervalSeconds, 1));

        if (this.requestThrottleService.isThrottled(
                "application-token",
                apiKey,
                resolveLimit(this.tokenMaxAttempts, 10),
                window,
                minInterval)
                || this.requestThrottleService.isThrottled(
                "application-token-ip",
                this.clientRequestContext.clientIp(),
                resolveLimit(this.tokenIpMaxAttempts, 30),
                ipWindow,
                ipMinInterval)) {
            throw new TooManyRequestsException();
        }
    }

    private void recordFailure(String apiKey) {
        this.requestThrottleService.recordAttempt(
                "application-token",
                apiKey,
                Duration.ofSeconds(resolveLimit(this.tokenWindowSeconds, 900))
        );
        this.requestThrottleService.recordAttempt(
                "application-token-ip",
                this.clientRequestContext.clientIp(),
                Duration.ofSeconds(resolveLimit(this.tokenIpWindowSeconds, 900))
        );
    }

    private int resolveLimit(Integer configuredValue, int fallback) {
        return configuredValue != null && configuredValue > 0 ? configuredValue : fallback;
    }

    private long resolveApplicationJwtExpiration(ClientApplication clientApplication) {
        if (clientApplication.getJwtExpirationInMinutes() != null && clientApplication.getJwtExpirationInMinutes() > 0) {
            return clientApplication.getJwtExpirationInMinutes();
        }

        return this.resolveDefaultAccessMinutes();
    }

    private long resolveDefaultAccessMinutes() {
        if (this.tokenExpirationMinutes != null && this.tokenExpirationMinutes > 0) {
            return this.tokenExpirationMinutes;
        }
        return 15L;
    }
}
