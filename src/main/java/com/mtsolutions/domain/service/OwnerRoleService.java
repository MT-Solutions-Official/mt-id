package com.mtsolutions.domain.service;

import com.mtsolutions.application.exception.OwnerRoleAlreadyExistsException;
import com.mtsolutions.domain.dto.request.UpdateOwnerRoleRequestDto;
import com.mtsolutions.domain.dto.request.CreateOwnerRoleRequestDto;
import com.mtsolutions.domain.entity.OwnerRole;
import com.mtsolutions.domain.repository.OwnerRoleRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Locale;

@ApplicationScoped
@Slf4j
public class OwnerRoleService {

    private final OwnerRoleRepository ownerRoleRepository;

    public OwnerRoleService(OwnerRoleRepository ownerRoleRepository) {
        this.ownerRoleRepository = ownerRoleRepository;
    }

    public OwnerRole createOwnerRole(CreateOwnerRoleRequestDto request) {
        String normalizedRoleName = this.normalizeRoleName(request.roleName());

        if (this.ownerRoleRepository.existsByRoleName(normalizedRoleName)) {
            throw new OwnerRoleAlreadyExistsException(normalizedRoleName);
        }

        OwnerRole ownerRole = OwnerRole.builder()
                .roleName(normalizedRoleName)
                .build();

        this.ownerRoleRepository.persist(ownerRole);
        log.info("Owner role created with ID: {}", ownerRole.getOwnerRoleId());

        return ownerRole;
    }

    public OwnerRole findOwnerRoleByName(String roleName) {
        String normalizedRoleName = this.normalizeRoleName(roleName);
        return this.ownerRoleRepository.findOwnerRoleByName(normalizedRoleName);
    }

    public OwnerRole findOwnerRoleById(String ownerRoleId) {
        return this.ownerRoleRepository.findOwnerRoleById(ownerRoleId);
    }

    public List<OwnerRole> findAllOwnerRoles() {
        return this.ownerRoleRepository.findAllOwnerRoles();
    }

    public OwnerRole updateOwnerRole(UpdateOwnerRoleRequestDto request) {
        OwnerRole ownerRole = this.ownerRoleRepository.findOwnerRoleById(request.ownerRoleId());
        String normalizedRoleName = this.normalizeRoleName(request.roleName());

        this.ownerRoleRepository.findByRoleName(normalizedRoleName)
                .filter(existingRole -> !existingRole.getOwnerRoleId().equals(ownerRole.getOwnerRoleId()))
                .ifPresent(existingRole -> {
                    throw new OwnerRoleAlreadyExistsException(normalizedRoleName);
                });

        ownerRole.setRoleName(normalizedRoleName);
        this.ownerRoleRepository.persistOrUpdate(ownerRole);
        log.info("Owner role updated with ID: {}", ownerRole.getOwnerRoleId());

        return ownerRole;
    }

    public void deleteOwnerRole(String ownerRoleId) {
        OwnerRole ownerRole = this.ownerRoleRepository.findOwnerRoleById(ownerRoleId);
        this.ownerRoleRepository.delete(ownerRole);
        log.info("Owner role deleted with ID: {}", ownerRoleId);
    }

    protected String normalizeRoleName(String roleName) {
        return roleName.trim().toUpperCase(Locale.ROOT);
    }
}
