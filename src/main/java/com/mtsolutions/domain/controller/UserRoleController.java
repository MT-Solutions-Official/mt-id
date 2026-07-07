package com.mtsolutions.domain.controller;

import com.mtsolutions.domain.dto.request.CreateUserRoleRequestDto;
import com.mtsolutions.domain.dto.request.UpdateUserRoleRequestDto;
import com.mtsolutions.domain.entity.UserRole;
import com.mtsolutions.domain.service.UserRoleService;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class UserRoleController {

    private final UserRoleService userRoleService;

    public UserRoleController(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    public UserRole createUserRole(CreateUserRoleRequestDto request, String authenticatedAppId) {
        return this.userRoleService.createUserRole(request, authenticatedAppId);
    }

    public UserRole findUserRoleById(String userRoleId, String authenticatedAppId) {
        return this.userRoleService.findUserRoleById(userRoleId, authenticatedAppId);
    }

    public List<UserRole> findUserRolesByAppId(String appId, String authenticatedAppId) {
        return this.userRoleService.findUserRolesByAppId(appId, authenticatedAppId);
    }

    public UserRole updateUserRole(UpdateUserRoleRequestDto request, String authenticatedAppId) {
        return this.userRoleService.updateUserRole(request, authenticatedAppId);
    }

    public void deleteUserRole(String userRoleId, String authenticatedAppId) {
        this.userRoleService.deleteUserRole(userRoleId, authenticatedAppId);
    }
}
