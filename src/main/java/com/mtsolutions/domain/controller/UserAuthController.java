package com.mtsolutions.domain.controller;

import com.mtsolutions.domain.dto.response.UserTokenResponseDto;
import com.mtsolutions.domain.service.UserAuthService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserAuthController {

    private final UserAuthService userAuthService;

    public UserAuthController(UserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    public UserTokenResponseDto generateUserToken(String email, String password) {
        return this.userAuthService.generateUserToken(email, password);
    }

    public UserTokenResponseDto generateGoogleUserToken(String idToken) {
        return this.userAuthService.generateGoogleUserToken(idToken);
    }

    public UserTokenResponseDto refreshUserToken() {
        return this.userAuthService.refreshUserToken();
    }

    public void logoutUser() {
        this.userAuthService.logoutUser();
    }
}
