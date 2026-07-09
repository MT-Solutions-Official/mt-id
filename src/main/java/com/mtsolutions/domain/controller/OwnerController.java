package com.mtsolutions.domain.controller;

import com.mtsolutions.domain.dto.request.CreateOwnerRequestDto;
import com.mtsolutions.domain.entity.Owner;
import com.mtsolutions.domain.service.OwnerService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OwnerController {

    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    public Owner createOwner(CreateOwnerRequestDto request) {
        return this.ownerService.createOwner(request);
    }
}
