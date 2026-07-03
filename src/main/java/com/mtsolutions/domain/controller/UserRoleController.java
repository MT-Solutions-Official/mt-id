package com.mtsolutions.domain.controller;

import com.mtsolutions.domain.dto.request.CreateUserRoleRequestDto;
import com.mtsolutions.domain.entity.UserRole;
import com.mtsolutions.domain.service.UserRoleService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRoleController {

    private final UserRoleService userRoleService;

    public UserRoleController(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    public UserRole createUserRole(CreateUserRoleRequestDto request) {
        return this.userRoleService.createUserRole(request);
    }
}
