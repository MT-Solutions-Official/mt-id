package com.mtsolutions.domain.controller;

import com.mtsolutions.domain.dto.response.OwnerTokenResponseDto;
import com.mtsolutions.domain.service.OwnerAuthService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OwnerAuthController {

    private final OwnerAuthService ownerAuthService;

    public OwnerAuthController(OwnerAuthService ownerAuthService) {
        this.ownerAuthService = ownerAuthService;
    }

    public OwnerTokenResponseDto generateOwnerToken(String email, String password) {
        return this.ownerAuthService.generateOwnerToken(email, password);
    }

    public OwnerTokenResponseDto generateGoogleOwnerToken(String idToken, String nonce) {
        return this.ownerAuthService.generateGoogleOwnerToken(idToken, nonce);
    }

    public OwnerTokenResponseDto refreshOwnerToken() {
        return this.ownerAuthService.refreshOwnerToken();
    }

    public void logoutOwner() {
        this.ownerAuthService.logoutOwner();
    }
}
