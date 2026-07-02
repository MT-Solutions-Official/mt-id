package com.mtsolutions.domain.constant;

import lombok.Getter;

@Getter
public enum MaritalStatus {
    SINGLE("Single"),
    MARRIED("Married"),
    DIVORCED("Divorced"),
    WIDOWED("Widowed"),
    UNKNOWN("Unknown");

    private final String displayName;

    MaritalStatus(String displayName) {
        this.displayName = displayName;
    }

}
