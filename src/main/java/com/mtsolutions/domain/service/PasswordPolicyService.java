package com.mtsolutions.domain.service;

import com.mtsolutions.application.client.hibp.PwnedPasswordsClient;
import com.mtsolutions.application.exception.PwnedPasswordException;
import com.mtsolutions.application.exception.WeakPasswordException;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.regex.Pattern;

@ApplicationScoped
@Slf4j
public class PasswordPolicyService {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 72;
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9]");

    private final PwnedPasswordsClient pwnedPasswordsClient;

    @ConfigProperty(name = "app.mt.id.password.pwned.enabled", defaultValue = "true")
    boolean pwnedCheckEnabled;

    @ConfigProperty(name = "app.mt.id.password.pwned.fail-open", defaultValue = "true")
    boolean pwnedFailOpen;

    public PasswordPolicyService(@RestClient PwnedPasswordsClient pwnedPasswordsClient) {
        this.pwnedPasswordsClient = pwnedPasswordsClient;
    }

    public void validate(String password) {
        if (password == null
                || password.length() < MIN_LENGTH
                || password.length() > MAX_LENGTH
                || !UPPERCASE.matcher(password).find()
                || !LOWERCASE.matcher(password).find()
                || !DIGIT.matcher(password).find()
                || !SPECIAL.matcher(password).find()) {
            throw new WeakPasswordException();
        }
        this.rejectIfPwned(password);
    }

    private void rejectIfPwned(String password) {
        if (!this.pwnedCheckEnabled) {
            return;
        }

        String sha1;
        try {
            sha1 = sha1Hex(password);
        } catch (RuntimeException e) {
            log.error("Failed to hash password for pwned check", e);
            if (!this.pwnedFailOpen) {
                throw new PwnedPasswordException();
            }
            return;
        }

        String prefix = sha1.substring(0, 5);
        String suffix = sha1.substring(5);
        String body;
        try {
            body = this.pwnedPasswordsClient.range(prefix);
        } catch (RuntimeException e) {
            log.error("Have I Been Pwned range lookup failed", e);
            if (!this.pwnedFailOpen) {
                throw new PwnedPasswordException();
            }
            return;
        }

        if (body == null || body.isBlank()) {
            return;
        }
        for (String line : body.split("\\R")) {
            if (line == null || line.isBlank()) {
                continue;
            }
            String[] parts = line.split(":");
            if (parts.length >= 1 && suffix.equalsIgnoreCase(parts[0].trim())) {
                throw new PwnedPasswordException();
            }
        }
    }

    private String sha1Hex(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hashed = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().withUpperCase().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 is required for pwned password checks", e);
        }
    }
}
