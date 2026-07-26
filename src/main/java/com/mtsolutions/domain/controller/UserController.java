package com.mtsolutions.domain.controller;

import com.mtsolutions.domain.dto.request.CreateAddressRequestDto;
import com.mtsolutions.domain.dto.request.CreateUserRequestDto;
import com.mtsolutions.domain.dto.request.RemoveUserImageRequestDto;
import com.mtsolutions.domain.dto.request.UploadUserImageRequestDto;
import com.mtsolutions.domain.entity.User;
import com.mtsolutions.domain.service.AccountEmailService;
import com.mtsolutions.domain.service.UserService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserController {

    private final UserService userService;
    private final AccountEmailService accountEmailService;

    public UserController(UserService userService, AccountEmailService accountEmailService) {
        this.userService = userService;
        this.accountEmailService = accountEmailService;
    }

    public User createUser(CreateUserRequestDto request) {
        return this.userService.createUser(request);
    }

    public User attachAddressToUser(String userId, CreateAddressRequestDto request) {
        return this.userService.attachAddressToUser(userId, request);
    }

    public void removeAddressFromUser(String userId, Integer addressIndex) {
        this.userService.removeAddressFromUser(userId, addressIndex);
    }

    public User uploadUserImage(UploadUserImageRequestDto request) {
        return this.userService.uploadUserImage(request);
    }

    public User removeUserImage(RemoveUserImageRequestDto request) {
        return this.userService.removeUserImage(request);
    }

    public void sendEmailVerification(String userId, String email) {
        this.accountEmailService.sendUserVerificationEmail(userId, email);
    }

    public void verifyEmail(String token) {
        this.accountEmailService.verifyUserEmail(token);
    }

    public void forgotPassword(String email, String appId) {
        this.accountEmailService.sendUserPasswordResetEmail(email, appId);
    }

    public void resetPassword(String token, String newPassword) {
        this.accountEmailService.resetUserPassword(token, newPassword);
    }
}
