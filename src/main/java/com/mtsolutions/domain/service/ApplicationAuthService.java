package com.mtsolutions.domain.service;

import com.mtsolutions.application.exception.ApplicationAuthenticationFailedException;
import com.mtsolutions.domain.dto.response.AppTokenResponseDto;
import com.mtsolutions.domain.entity.ClientApplication;
import com.mtsolutions.domain.repository.ClientApplicationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;

@ApplicationScoped
public class ApplicationAuthService {

    @ConfigProperty(name = "app.mt.id.token.expiration.hours")
    Integer tokenExpirationInHours;

    private final ClientApplicationRepository clientApplicationRepository;
    private final BcryptService bcryptService;
    private final JwtService jwtService;

    public ApplicationAuthService(ClientApplicationRepository clientApplicationRepository,
                                  BcryptService bcryptService,
                                  JwtService jwtService) {
        this.clientApplicationRepository = clientApplicationRepository;
        this.bcryptService = bcryptService;
        this.jwtService = jwtService;
    }

    public AppTokenResponseDto generateApplicationToken(String apiKey, String apiSecret) {
        String normalizedApiKey = apiKey != null ? apiKey.trim() : null;
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

        Duration expiration = Duration.ofMinutes(resolveApplicationJwtExpiration(clientApplication));
        return this.jwtService.generateApplicationToken(clientApplication, expiration);
    }

    private long resolveApplicationJwtExpiration(ClientApplication clientApplication) {
        if (clientApplication.getJwtExpirationInMinutes() != null && clientApplication.getJwtExpirationInMinutes() > 0) {
            return clientApplication.getJwtExpirationInMinutes();
        }

        return this.tokenExpirationInHours * 60L;
    }
}
