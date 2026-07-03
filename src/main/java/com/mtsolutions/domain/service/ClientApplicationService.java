package com.mtsolutions.domain.service;

import com.mtsolutions.application.exception.ApplicationOwnerNotFoundException;
import com.mtsolutions.application.utils.DateUtils;
import com.mtsolutions.application.utils.KeyGeneratorUtils;
import com.mtsolutions.domain.dto.AddOwnersToClientApplicationRequestDto;
import com.mtsolutions.domain.dto.CreateClientApplicationRequestDto;
import com.mtsolutions.domain.dto.UpdateRequiredUserFieldsRequestDto;
import com.mtsolutions.domain.entity.ApplicationOwner;
import com.mtsolutions.domain.entity.ClientApplication;
import com.mtsolutions.domain.repository.ClientApplicationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@ApplicationScoped
@Slf4j
public class ClientApplicationService {

    private final ClientApplicationRepository clientApplicationRepository;
    private final ApplicationOwnerService applicationOwnerService;
    private final KeyGeneratorUtils keyGeneratorUtils;
    private final BcryptService bcryptService;
    private final DateUtils dateUtils;

    public ClientApplicationService(ClientApplicationRepository clientApplicationRepository, ApplicationOwnerService applicationOwnerService, KeyGeneratorUtils keyGeneratorUtils, BcryptService bcryptService, DateUtils dateUtils) {
        this.clientApplicationRepository = clientApplicationRepository;
        this.applicationOwnerService = applicationOwnerService;
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
                .requiredUserFields(request.requiredUserFields() != null ? request.requiredUserFields() : new ArrayList<>())
                .createdAt(this.dateUtils.now())
                .updatedAt(this.dateUtils.now())
                .active(true)
                .build();

        this.clientApplicationRepository.persist(clientApplication);
        log.info("Client application created with ID: {}", clientApplication.getAppId());

        return clientApplication;
    }

    public ClientApplication findClientApplicationById(String appId) {
        return this.clientApplicationRepository.findClientApplicationById(appId);
    }

    public void addOwnersToClientApplication(AddOwnersToClientApplicationRequestDto request) {
        ClientApplication clientApplication = this.findClientApplicationById(request.appId());
        if (clientApplication.getOwners() == null) {
            clientApplication.setOwners(new ArrayList<>());
        }

        for (String ownerId : request.ownerIds()) {
            if (Boolean.FALSE.equals(this.applicationOwnerService.existsApplicationOwnerById(ownerId))) {
                throw new ApplicationOwnerNotFoundException();
            }
            ApplicationOwner owner = this.applicationOwnerService.findApplicationOwnerById(ownerId);
            
            if (clientApplication.getOwners().stream().noneMatch(o -> o.getOwnerId().equals(owner.getOwnerId()))) {
                clientApplication.getOwners().add(owner);
            }
        }

        this.clientApplicationRepository.persistOrUpdate(clientApplication);

        log.info("Owners added to client application with ID: {}", clientApplication.getAppId());
    }

    public void updateRequiredUserFields(UpdateRequiredUserFieldsRequestDto request) {
        ClientApplication clientApplication = this.findClientApplicationById(request.appId());
        clientApplication.setRequiredUserFields(request.requiredUserFields() != null ? request.requiredUserFields() : new ArrayList<>());
        clientApplication.setUpdatedAt(this.dateUtils.now());

        this.clientApplicationRepository.persistOrUpdate(clientApplication);
        log.info("Required user fields updated for client application with ID: {}", clientApplication.getAppId());
    }
}
