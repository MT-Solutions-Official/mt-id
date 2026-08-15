package com.mtsolutions.domain.controller;

import com.mtsolutions.domain.dto.request.CreateOwnerRequestDto;
import com.mtsolutions.domain.entity.Owner;
import com.mtsolutions.domain.service.AccountEmailService;
import com.mtsolutions.domain.service.OwnerService;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class OwnerController {

    private final OwnerService ownerService;
    private final AccountEmailService accountEmailService;

    public OwnerController(OwnerService ownerService, AccountEmailService accountEmailService) {
        this.ownerService = ownerService;
        this.accountEmailService = accountEmailService;
    }

    public Owner createOwner(CreateOwnerRequestDto request) {
        Owner owner = this.ownerService.createOwner(request);
        this.accountEmailService.sendOwnerVerificationEmailAfterCreate(owner);
        return owner;
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

    public Owner findCurrentOwner() {
        return this.ownerService.findCurrentOwner();
    }

    public List<Owner> listOwners() {
        return this.ownerService.listOwners();
    }

    public Owner findOwnerForConsole(String ownerId) {
        return this.ownerService.findOwnerForConsole(ownerId);
    }

    public Owner disableOwner(String ownerId) {
        return this.ownerService.disableOwner(ownerId);
    }

    public Owner enableOwner(String ownerId) {
        return this.ownerService.enableOwner(ownerId);
    }
}
