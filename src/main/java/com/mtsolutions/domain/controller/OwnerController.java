package com.mtsolutions.domain.controller;

import com.mtsolutions.domain.constant.ImageType;
import com.mtsolutions.domain.dto.request.CreateAddressRequestDto;
import com.mtsolutions.domain.dto.request.CreateOwnerRequestDto;
import com.mtsolutions.domain.dto.request.UpdateOwnerRequestDto;
import com.mtsolutions.domain.dto.request.UploadOwnerImageRequestDto;
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

    public Owner updateCurrentOwner(UpdateOwnerRequestDto request) {
        return this.ownerService.updateCurrentOwner(request);
    }

    public Owner attachAddressToCurrentOwner(CreateAddressRequestDto request) {
        return this.ownerService.attachAddressToCurrentOwner(request);
    }

    public Owner removeAddressFromCurrentOwner(Integer addressIndex) {
        return this.ownerService.removeAddressFromCurrentOwner(addressIndex);
    }

    public Owner uploadCurrentOwnerImage(UploadOwnerImageRequestDto request) {
        return this.ownerService.uploadCurrentOwnerImage(request);
    }

    public Owner removeCurrentOwnerImage(ImageType imageType) {
        return this.ownerService.removeCurrentOwnerImage(imageType);
    }
}
