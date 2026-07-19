package com.mtsolutions.domain.service;

import com.mtsolutions.application.common.ContextComponent;
import com.mtsolutions.application.exception.ApplicationForbiddenException;
import com.mtsolutions.application.exception.OwnerNotFoundException;
import com.mtsolutions.application.utils.DateUtils;
import com.mtsolutions.application.utils.KeyGeneratorUtils;
import com.mtsolutions.domain.dto.request.AddOwnersToClientApplicationRequestDto;
import com.mtsolutions.domain.dto.request.CreateClientApplicationRequestDto;
import com.mtsolutions.domain.dto.request.UpdateRequiredUserFieldsRequestDto;
import com.mtsolutions.domain.entity.ClientApplication;
import com.mtsolutions.domain.entity.Owner;
import com.mtsolutions.domain.model.ClientApplicationSecretResult;
import com.mtsolutions.domain.repository.ClientApplicationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@Slf4j
public class ClientApplicationService {

    private final ClientApplicationRepository clientApplicationRepository;
    private final OwnerService ownerService;
    private final KeyGeneratorUtils keyGeneratorUtils;
    private final BcryptService bcryptService;
    private final DateUtils dateUtils;
    private final ContextComponent contextComponent;

    public ClientApplicationService(ClientApplicationRepository clientApplicationRepository, OwnerService ownerService, KeyGeneratorUtils keyGeneratorUtils, BcryptService bcryptService, DateUtils dateUtils, ContextComponent contextComponent) {
        this.clientApplicationRepository = clientApplicationRepository;
        this.ownerService = ownerService;
        this.keyGeneratorUtils = keyGeneratorUtils;
        this.bcryptService = bcryptService;
        this.dateUtils = dateUtils;
        this.contextComponent = contextComponent;
    }

    public ClientApplicationSecretResult createClientApplication(CreateClientApplicationRequestDto request) {
        log.info("Creating client application with name: {}", request.name());

        this.validateOwnerAccess(request.ownerId(), null);

        if (Boolean.FALSE.equals(this.ownerService.existsOwnerById(request.ownerId()))) {
            throw new OwnerNotFoundException();
        }

        Owner owner = this.ownerService.findOwnerById(request.ownerId());
        String apiKey = this.keyGeneratorUtils.generateApiKey();
        String apiSecret = this.keyGeneratorUtils.generateApiSecret();
        String hashedApiSecret = this.bcryptService.encryptPassword(apiSecret);

        ClientApplication clientApplication = ClientApplication.builder()
                .owners(new ArrayList<>(List.of(owner)))
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

        return new ClientApplicationSecretResult(clientApplication, apiSecret);
    }

    public ClientApplication findClientApplicationById(String appId) {
        return this.clientApplicationRepository.findClientApplicationById(appId);
    }

    public void addOwnersToClientApplication(AddOwnersToClientApplicationRequestDto request) {
        ClientApplication clientApplication = this.findClientApplicationById(request.appId());
        this.validateOwnerAccess(null, clientApplication);
        if (clientApplication.getOwners() == null) {
            clientApplication.setOwners(new ArrayList<>());
        }

        for (String ownerId : request.ownerIds()) {
            Owner owner = this.ownerService.findOwnerById(ownerId);

            if (clientApplication.getOwners().stream().noneMatch(o -> o.getOwnerId().equals(owner.getOwnerId()))) {
                clientApplication.getOwners().add(owner);
            }
        }

        this.clientApplicationRepository.persistOrUpdate(clientApplication);

        log.info("Owners added to client application with ID: {}", clientApplication.getAppId());
    }

    public void updateRequiredUserFields(UpdateRequiredUserFieldsRequestDto request) {
        ClientApplication clientApplication = this.findClientApplicationById(request.appId());
        this.validateOwnerAccess(null, clientApplication);
        clientApplication.setRequiredUserFields(request.requiredUserFields() != null ? request.requiredUserFields() : new ArrayList<>());
        clientApplication.setUpdatedAt(this.dateUtils.now());

        this.clientApplicationRepository.persistOrUpdate(clientApplication);
        log.info("Required user fields updated for client application with ID: {}", clientApplication.getAppId());
    }

    public ClientApplicationSecretResult rotateClientApplicationSecret() {
        if (!"APPLICATION".equals(this.contextComponent.getRole())) {
            throw new ApplicationForbiddenException();
        }

        String appId = this.contextComponent.getAuthenticatedAppId();
        ClientApplication clientApplication = this.findClientApplicationById(appId);
        String apiSecret = this.keyGeneratorUtils.generateApiSecret();
        String hashedApiSecret = this.bcryptService.encryptPassword(apiSecret);

        clientApplication.setApiSecret(hashedApiSecret);
        clientApplication.setUpdatedAt(this.dateUtils.now());
        this.clientApplicationRepository.persistOrUpdate(clientApplication);

        log.info("Client application secret rotated for ID: {}", clientApplication.getAppId());
        return new ClientApplicationSecretResult(clientApplication, apiSecret);
    }

    private void validateOwnerAccess(String ownerId, ClientApplication clientApplication) {
        if (!"OWNER_WRITER".equals(this.contextComponent.getRole())) {
            throw new ApplicationForbiddenException();
        }

        String authenticatedOwnerId = this.contextComponent.getAuthenticatedOwnerId();
        if (ownerId != null && !authenticatedOwnerId.equals(ownerId)) {
            throw new ApplicationForbiddenException();
        }

        if (clientApplication != null && clientApplication.getOwners() != null
                && clientApplication.getOwners().stream().noneMatch(owner -> authenticatedOwnerId.equals(owner.getOwnerId()))) {
            throw new ApplicationForbiddenException();
        }
    }
}
