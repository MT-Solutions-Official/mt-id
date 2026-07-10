package com.mtsolutions.domain.controller;

import com.mtsolutions.domain.dto.response.AppTokenResponseDto;
import com.mtsolutions.domain.dto.response.OwnerTokenResponseDto;
import com.mtsolutions.domain.dto.response.UserTokenResponseDto;
import com.mtsolutions.domain.service.ApplicationAuthService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApplicationAuthController {

    private final ApplicationAuthService applicationAuthService;

    public ApplicationAuthController(ApplicationAuthService applicationAuthService) {
        this.applicationAuthService = applicationAuthService;
    }

    public OwnerTokenResponseDto generateOwnerToken(String email, String password) {
        return this.applicationAuthService.generateOwnerToken(email, password);
    }

    public AppTokenResponseDto generateApplicationToken(String apiKey, String apiSecret) {
        return this.applicationAuthService.generateApplicationToken(apiKey, apiSecret);
    }

    public UserTokenResponseDto generateUserToken(String email, String password) {
        return this.applicationAuthService.generateUserToken(email, password);
    }

    public OwnerTokenResponseDto refreshOwnerToken() {
        return this.applicationAuthService.refreshOwnerToken();
    }

    public UserTokenResponseDto refreshUserToken() {
        return this.applicationAuthService.refreshUserToken();
    }

    public void logoutOwner() {
        this.applicationAuthService.logoutOwner();
    }

    public void logoutUser() {
        this.applicationAuthService.logoutUser();
    }
}
