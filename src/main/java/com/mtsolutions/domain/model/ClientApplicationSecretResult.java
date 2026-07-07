package com.mtsolutions.domain.model;

import com.mtsolutions.domain.entity.ClientApplication;

public record ClientApplicationSecretResult(
        ClientApplication clientApplication,
        String apiSecret
) {
}
