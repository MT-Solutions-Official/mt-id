package com.mtsolutions.domain.controller;

import com.mtsolutions.domain.dto.response.AppTokenResponseDto;
import com.mtsolutions.domain.service.ApplicationAuthService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApplicationAuthController {

    private final ApplicationAuthService applicationAuthService;

    public ApplicationAuthController(ApplicationAuthService applicationAuthService) {
        this.applicationAuthService = applicationAuthService;
    }

    public AppTokenResponseDto generateOwnerToken(String email, String password) {
        return this.applicationAuthService.generateOwnerToken(email, password);
    }
}
