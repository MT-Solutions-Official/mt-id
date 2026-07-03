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

    public UserRole createUserRole(CreateUserRoleRequestDto request) {
        return this.userRoleService.createUserRole(request);
    }

    public UserRole findUserRoleById(String userRoleId) {
        return this.userRoleService.findUserRoleById(userRoleId);
    }

    public List<UserRole> findUserRolesByAppId(String appId) {
        return this.userRoleService.findUserRolesByAppId(appId);
    }

    public UserRole updateUserRole(UpdateUserRoleRequestDto request) {
        return this.userRoleService.updateUserRole(request);
    }

    public void deleteUserRole(String userRoleId) {
        this.userRoleService.deleteUserRole(userRoleId);
    }
}
