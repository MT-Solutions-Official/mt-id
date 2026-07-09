package com.mtsolutions.application.constant;

import lombok.Getter;

@Getter
public enum CloudinaryFolder {

    USER_PICTURE("/mt-id/users/pictures", "image"),
    USER_DOCUMENT("/mt-id/users/documents", "raw");


    private final String path;
    private final String resourceType;

    CloudinaryFolder(String path, String resourceType) {
        this.path = path;
        this.resourceType = resourceType;
    }
}
