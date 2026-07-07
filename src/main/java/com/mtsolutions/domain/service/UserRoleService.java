package com.mtsolutions.domain.service;

import com.mtsolutions.application.exception.UserRoleAlreadyExistsException;
import com.mtsolutions.application.exception.UserRoleNotFoundException;
import com.mtsolutions.application.exception.ApplicationForbiddenException;
import com.mtsolutions.domain.dto.request.CreateUserRoleRequestDto;
import com.mtsolutions.domain.dto.request.UpdateUserRoleRequestDto;
import com.mtsolutions.domain.entity.UserRole;
import com.mtsolutions.domain.repository.UserRoleRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final ClientApplicationService clientApplicationService;

    public UserRoleService(UserRoleRepository userRoleRepository, ClientApplicationService clientApplicationService) {
        this.userRoleRepository = userRoleRepository;
        this.clientApplicationService = clientApplicationService;
    }

    public UserRole createUserRole(CreateUserRoleRequestDto request, String authenticatedAppId) {
        this.clientApplicationService.findClientApplicationById(authenticatedAppId);
        String normalizedRoleName = this.normalizeRoleName(request.roleName());

        if (this.userRoleRepository.existsByAppIdAndRoleName(authenticatedAppId, normalizedRoleName)) {
            throw new UserRoleAlreadyExistsException(normalizedRoleName);
        }

        UserRole userRole = UserRole.builder()
                .appId(authenticatedAppId)
                .roleName(normalizedRoleName)
                .build();

        this.userRoleRepository.persist(userRole);
        log.info("User role created with ID: {}", userRole.getUserRoleId());

        return userRole;
    }

    public UserRole findUserRoleById(String userRoleId, String authenticatedAppId) {
        UserRole userRole = this.userRoleRepository.findUserRoleById(userRoleId);
        this.validateRoleOwnership(userRole, authenticatedAppId);

        return userRole;
    }

    public List<UserRole> findUserRolesByAppId(String appId, String authenticatedAppId) {
        if (!authenticatedAppId.equals(appId)) {
            throw new ApplicationForbiddenException();
        }
        this.clientApplicationService.findClientApplicationById(authenticatedAppId);
        return this.userRoleRepository.findByAppId(authenticatedAppId);
    }

    public UserRole updateUserRole(UpdateUserRoleRequestDto request, String authenticatedAppId) {
        UserRole userRole = this.userRoleRepository.findUserRoleById(request.userRoleId());
        this.validateRoleOwnership(userRole, authenticatedAppId);
        String normalizedRoleName = this.normalizeRoleName(request.roleName());

        this.userRoleRepository.findByAppIdAndRoleName(userRole.getAppId(), normalizedRoleName)
                .filter(existingRole -> !existingRole.getUserRoleId().equals(userRole.getUserRoleId()))
                .ifPresent(existingRole -> {
                    throw new UserRoleAlreadyExistsException(normalizedRoleName);
                });

        userRole.setRoleName(normalizedRoleName);
        this.userRoleRepository.persistOrUpdate(userRole);
        log.info("User role updated with ID: {}", userRole.getUserRoleId());

        return userRole;
    }

    public void deleteUserRole(String userRoleId, String authenticatedAppId) {
        UserRole userRole = this.userRoleRepository.findUserRoleById(userRoleId);
        this.validateRoleOwnership(userRole, authenticatedAppId);
        this.userRoleRepository.delete(userRole);
        log.info("User role deleted with ID: {}", userRoleId);
    }

    public List<String> resolveRoleIdsByAppIdAndRoleNames(String appId, List<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> normalizedRoleNames = roleNames.stream()
                .filter(roleName -> roleName != null && !roleName.isBlank())
                .map(this::normalizeRoleName)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (normalizedRoleNames.isEmpty()) {
            return new ArrayList<>();
        }

        List<UserRole> existingRoles = this.userRoleRepository.findByAppIdAndRoleNames(appId, new ArrayList<>(normalizedRoleNames));
        Set<String> existingRoleNames = existingRoles.stream()
                .map(UserRole::getRoleName)
                .collect(Collectors.toSet());

        for (String roleName : normalizedRoleNames) {
            if (!existingRoleNames.contains(roleName)) {
                throw new UserRoleNotFoundException(roleName);
            }
        }

        return existingRoles.stream()
                .map(UserRole::getUserRoleId)
                .toList();
    }

    private String normalizeRoleName(String roleName) {
        return roleName.trim().toUpperCase(Locale.ROOT);
    }

    private void validateRoleOwnership(UserRole userRole, String authenticatedAppId) {
        if (!authenticatedAppId.equals(userRole.getAppId())) {
            throw new ApplicationForbiddenException();
        }
    }
}
