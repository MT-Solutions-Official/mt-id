package com.mtsolutions.domain.controller;

import com.mtsolutions.domain.dto.CreateUserRequestDto;
import com.mtsolutions.domain.entity.User;
import com.mtsolutions.domain.service.UserService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public User createUser(CreateUserRequestDto request) {
        return this.userService.createUser(request);
    }
}
