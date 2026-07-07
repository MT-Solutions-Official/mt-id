package com.mtsolutions.domain.constant;

import lombok.Getter;

@Getter
public enum Errors {

    APPLICATION_OWNER_NOT_FOUND("Application owner not found"),
    CLIENT_APPLICATION_NOT_FOUND("Client application not found"),
    APPLICATION_AUTHENTICATION_FAILED("Application authentication failed"),
    APPLICATION_FORBIDDEN("Application forbidden"),
    REQUIRED_USER_FIELD_MISSING("Required user field is missing"),
    OWNER_ROLE_ALREADY_EXISTS("Owner role already exists"),
    OWNER_ROLE_NOT_FOUND("Owner role not found"),
    USER_ROLE_ALREADY_EXISTS("User role already exists"),
    USER_ROLE_NOT_FOUND("User role not found"),
    FORBIDDEN("Forbidden");

    private final String displayName;

    Errors(String displayName) {
        this.displayName = displayName;
    }

}
