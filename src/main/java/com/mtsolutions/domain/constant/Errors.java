package com.mtsolutions.domain.constant;

import lombok.Getter;

@Getter
public enum Errors {

    OWNER_NOT_FOUND("Owner not found"),
    CLIENT_APPLICATION_NOT_FOUND("Client application not found"),
    APPLICATION_AUTHENTICATION_FAILED("Application authentication failed"),
    APPLICATION_FORBIDDEN("Application forbidden"),
    REQUIRED_USER_FIELD_MISSING("Required user field is missing"),
    USER_ROLE_ALREADY_EXISTS("User role already exists"),
    EMAIL_ALREADY_EXISTS("Email already exists"),
    USERNAME_ALREADY_EXISTS("Username already exists"),
    USER_ROLE_NOT_FOUND("User role not found"),
    USER_NOT_FOUND("User not found"),
    FORBIDDEN("Forbidden"),
    VIACEP_NOT_FOUND("ViaCEP not found"),
    VIACEP_INVALID_CEP("ViaCEP invalid CEP"),
    VIACEP_API_UNAVAILABLE("ViaCEP API unavailable"),
    KODEPOS_ZIP_NOT_FOUND("KodePos zip not found"),
    KODEPOS_API_UNAVAILABLE("KodePos API unavailable"),
    ZIPPOPOTAM_ZIP_NOT_FOUND("Zippopotam zip not found"),
    ZIPPOPOTAM_API_UNAVAILABLE("Zippopotam API unavailable"),
    EMAIL_ALREADY_VERIFIED("Email is already verified"),
    EMAIL_NOT_VERIFIED("Email is not verified"),
    ACCOUNT_DISABLED("Account is disabled"),
    WEAK_PASSWORD("Password must be at least 8 characters and include uppercase, lowercase, a number and a special character"),
    PASSWORD_COMPROMISED("Password has appeared in a data breach and cannot be used"),
    INVALID_OR_EXPIRED_TOKEN("Invalid or expired token"),
    RATE_LIMIT_EXCEEDED("Too many requests. Please try again later.");

    private final String displayName;

    Errors(String displayName) {
        this.displayName = displayName;
    }

}
