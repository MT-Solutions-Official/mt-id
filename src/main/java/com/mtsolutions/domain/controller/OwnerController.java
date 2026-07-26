package com.mtsolutions.domain.controller;

import com.mtsolutions.domain.dto.request.CreateOwnerRequestDto;
import com.mtsolutions.domain.entity.Owner;
import com.mtsolutions.domain.service.AccountEmailService;
import com.mtsolutions.domain.service.OwnerService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OwnerController {

    private final OwnerService ownerService;
    private final AccountEmailService accountEmailService;

    public OwnerController(OwnerService ownerService, AccountEmailService accountEmailService) {
        this.ownerService = ownerService;
        this.accountEmailService = accountEmailService;
    }

    public Owner createOwner(CreateOwnerRequestDto request) {
        return this.ownerService.createOwner(request);
    }

    public void sendEmailVerification(String ownerId, String email) {
        this.accountEmailService.sendOwnerVerificationEmail(ownerId, email);
    }

    public void verifyEmail(String token) {
        this.accountEmailService.verifyOwnerEmail(token);
    }

    public void forgotPassword(String email) {
        this.accountEmailService.sendOwnerPasswordResetEmail(email);
    }

    public void resetPassword(String token, String newPassword) {
        this.accountEmailService.resetOwnerPassword(token, newPassword);
    }
}
