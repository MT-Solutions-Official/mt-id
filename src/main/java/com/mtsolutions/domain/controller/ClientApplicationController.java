package com.mtsolutions.domain.controller;

import com.mtsolutions.domain.dto.CreateClientApplicationRequestDto;
import com.mtsolutions.domain.entity.ClientApplication;
import com.mtsolutions.domain.service.ClientApplicationService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClientApplicationController {

    private final ClientApplicationService clientApplicationService;

    public ClientApplicationController(ClientApplicationService clientApplicationService) {
        this.clientApplicationService = clientApplicationService;
    }

    public ClientApplication createClientApplication(CreateClientApplicationRequestDto request) {
        return this.clientApplicationService.createClientApplication(request);
    }
}
