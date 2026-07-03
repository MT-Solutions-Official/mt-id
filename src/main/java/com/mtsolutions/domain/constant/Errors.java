package com.mtsolutions.domain.constant;

import lombok.Getter;

@Getter
public enum Errors {

    APPLICATION_OWNER_NOT_FOUND("Application owner not found"),
    CLIENT_APPLICATION_NOT_FOUND("Client application not found");

    private final String displayName;

    Errors(String displayName) {
        this.displayName = displayName;
    }

}
