package com.mtsolutions.domain.controller;

import com.mtsolutions.domain.dto.request.AddOwnersToClientApplicationRequestDto;
import com.mtsolutions.domain.dto.request.CreateClientApplicationRequestDto;
import com.mtsolutions.domain.dto.request.UpdateClientApplicationSettingsRequestDto;
import com.mtsolutions.domain.dto.request.UpdateRequiredUserFieldsRequestDto;
import com.mtsolutions.domain.entity.ClientApplication;
import com.mtsolutions.domain.model.ClientApplicationSecretResult;
import com.mtsolutions.domain.service.ClientApplicationService;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ClientApplicationController {

    private final ClientApplicationService clientApplicationService;

    public ClientApplicationController(ClientApplicationService clientApplicationService) {
        this.clientApplicationService = clientApplicationService;
    }

    public ClientApplicationSecretResult createClientApplication(CreateClientApplicationRequestDto request) {
        return this.clientApplicationService.createClientApplication(request);
    }

    public List<ClientApplication> listMyApplications() {
        return this.clientApplicationService.listMyApplications();
    }

    public ClientApplication findOwnedClientApplication(String appId) {
        return this.clientApplicationService.findOwnedClientApplication(appId);
    }

    public void addOwnersToClientApplication(AddOwnersToClientApplicationRequestDto request) {
        this.clientApplicationService.addOwnersToClientApplication(request);
    }

    public ClientApplication removeOwnerFromClientApplication(String appId, String ownerId) {
        return this.clientApplicationService.removeOwnerFromClientApplication(appId, ownerId);
    }

    public void updateRequiredUserFields(UpdateRequiredUserFieldsRequestDto request) {
        this.clientApplicationService.updateRequiredUserFields(request);
    }

    public ClientApplication updateSettings(UpdateClientApplicationSettingsRequestDto request) {
        return this.clientApplicationService.updateSettings(request);
    }

    public ClientApplication disableClientApplication(String appId) {
        return this.clientApplicationService.disableClientApplication(appId);
    }

    public ClientApplication enableClientApplication(String appId) {
        return this.clientApplicationService.enableClientApplication(appId);
    }

    public ClientApplicationSecretResult rotateClientApplicationSecret() {
        return this.clientApplicationService.rotateClientApplicationSecret();
    }

    public ClientApplicationSecretResult rotateOwnedClientApplicationSecret(String appId) {
        return this.clientApplicationService.rotateOwnedClientApplicationSecret(appId);
    }
}
