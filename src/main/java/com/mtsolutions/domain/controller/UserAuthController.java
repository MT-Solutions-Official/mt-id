package com.mtsolutions.domain.controller;

import com.mtsolutions.domain.dto.request.GenerateUserGoogleTokenRequestDto;
import com.mtsolutions.domain.dto.response.UserTokenResponseDto;
import com.mtsolutions.domain.service.UserAuthService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserAuthController {

    private final UserAuthService userAuthService;

    public UserAuthController(UserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    public UserTokenResponseDto generateUserToken(String email, String password, String appId) {
        return this.userAuthService.generateUserToken(email, password, appId);
    }

    public UserTokenResponseDto generateGoogleUserToken(GenerateUserGoogleTokenRequestDto request) {
        return this.userAuthService.generateGoogleUserToken(request);
    }

    public UserTokenResponseDto refreshUserToken() {
        return this.userAuthService.refreshUserToken();
    }

    public void logoutUser() {
        this.userAuthService.logoutUser();
    }
}
