package com.mtsolutions.domain.controller;

import com.mtsolutions.domain.dto.CreateApplicationOwnerRequestDto;
import com.mtsolutions.domain.entity.ApplicationOwner;
import com.mtsolutions.domain.service.ApplicationOwnerService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApplicationOwnerController {

    private final ApplicationOwnerService applicationOwnerService;

    public ApplicationOwnerController(ApplicationOwnerService applicationOwnerService) {
        this.applicationOwnerService = applicationOwnerService;
    }

    public ApplicationOwner createApplicationOwner(CreateApplicationOwnerRequestDto request) {
        return this.applicationOwnerService.createApplicationOwner(request);
    }
}
