package com.mtsolutions.domain.service;

import com.mtsolutions.application.utils.DateUtils;
import com.mtsolutions.application.utils.KeyGeneratorUtils;
import com.mtsolutions.domain.dto.CreateClientApplicationRequestDto;
import com.mtsolutions.domain.entity.ClientApplication;
import com.mtsolutions.domain.repository.ClientApplicationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class ClientApplicationService {

    private final ClientApplicationRepository clientApplicationRepository;
    private final KeyGeneratorUtils keyGeneratorUtils;
    private final BcryptService bcryptService;
    private final DateUtils dateUtils;

    public ClientApplicationService(ClientApplicationRepository clientApplicationRepository, KeyGeneratorUtils keyGeneratorUtils, BcryptService bcryptService, DateUtils dateUtils) {
        this.clientApplicationRepository = clientApplicationRepository;
        this.keyGeneratorUtils = keyGeneratorUtils;
        this.bcryptService = bcryptService;
        this.dateUtils = dateUtils;
    }

    public ClientApplication createClientApplication(CreateClientApplicationRequestDto request) {
        log.info("Creating client application with name: {}", request.name());

        String apiKey = this.keyGeneratorUtils.generateApiKey();
        String apiSecret = this.keyGeneratorUtils.generateApiSecret();
        String hashedApiSecret = this.bcryptService.encryptPassword(apiSecret);

        ClientApplication clientApplication = ClientApplication.builder()
                .name(request.name())
                .description(request.description())
                .apiKey(apiKey)
                .apiSecret(hashedApiSecret)
                .jwtExpirationInMinutes(request.jwtExpirationInMinutes())
                .refreshTokenExpirationInDays(request.refreshTokenExpirationInDays())
                .allowedOrigins(request.allowedOrigins())
                .createdAt(this.dateUtils.now())
                .updatedAt(this.dateUtils.now())
                .active(true)
                .build();

        this.clientApplicationRepository.persist(clientApplication);
        log.info("Client application created with ID: {}", clientApplication.getAppId());

        return clientApplication;
    }
}
