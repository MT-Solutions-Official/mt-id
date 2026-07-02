package com.mtsolutions.application.utils;

import jakarta.enterprise.context.ApplicationScoped;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@ApplicationScoped
public class KeyGeneratorUtils {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    public String generateApiKey() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return "mt_key_" + uuid;
    }

    public String generateApiSecret() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return "mt_sec_" + base64Encoder.encodeToString(randomBytes);
    }
}