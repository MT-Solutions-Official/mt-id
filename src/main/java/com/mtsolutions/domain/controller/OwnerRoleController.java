package com.mtsolutions.domain.controller;

import com.mtsolutions.domain.dto.request.CreateOwnerRoleRequestDto;
import com.mtsolutions.domain.dto.request.UpdateOwnerRoleRequestDto;
import com.mtsolutions.domain.entity.OwnerRole;
import com.mtsolutions.domain.service.OwnerRoleService;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class OwnerRoleController {

    private final OwnerRoleService ownerRoleService;

    public OwnerRoleController(OwnerRoleService ownerRoleService) {
        this.ownerRoleService = ownerRoleService;
    }

    public OwnerRole createOwnerRole(CreateOwnerRoleRequestDto request) {
        return this.ownerRoleService.createOwnerRole(request);
    }

    public OwnerRole findOwnerRoleById(String ownerRoleId) {
        return this.ownerRoleService.findOwnerRoleById(ownerRoleId);
    }

    public List<OwnerRole> findAllOwnerRoles() {
        return this.ownerRoleService.findAllOwnerRoles();
    }

    public OwnerRole updateOwnerRole(UpdateOwnerRoleRequestDto request) {
        return this.ownerRoleService.updateOwnerRole(request);
    }

    public void deleteOwnerRole(String ownerRoleId) {
        this.ownerRoleService.deleteOwnerRole(ownerRoleId);
    }
}
