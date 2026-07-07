package com.mtsolutions.domain.controller;

import com.mtsolutions.domain.dto.request.AddOwnersToClientApplicationRequestDto;
import com.mtsolutions.domain.dto.request.CreateClientApplicationRequestDto;
import com.mtsolutions.domain.dto.request.UpdateRequiredUserFieldsRequestDto;
import com.mtsolutions.domain.model.ClientApplicationSecretResult;
import com.mtsolutions.domain.service.ClientApplicationService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClientApplicationController {

    private final ClientApplicationService clientApplicationService;

    public ClientApplicationController(ClientApplicationService clientApplicationService) {
        this.clientApplicationService = clientApplicationService;
    }

    public ClientApplicationSecretResult createClientApplication(CreateClientApplicationRequestDto request) {
        return this.clientApplicationService.createClientApplication(request);
    }

    public void addOwnersToClientApplication(AddOwnersToClientApplicationRequestDto request) {
        this.clientApplicationService.addOwnersToClientApplication(request);
    }

    public void updateRequiredUserFields(UpdateRequiredUserFieldsRequestDto request) {
        this.clientApplicationService.updateRequiredUserFields(request);
    }

    public ClientApplicationSecretResult rotateClientApplicationSecret(String appId) {
        return this.clientApplicationService.rotateClientApplicationSecret(appId);
    }
}
