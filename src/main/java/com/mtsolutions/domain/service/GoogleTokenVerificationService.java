package com.mtsolutions.domain.service;

import com.mtsolutions.application.client.google.GoogleTokenInfoClient;
import com.mtsolutions.application.client.google.GoogleTokenInfoResponseDto;
import com.mtsolutions.application.exception.ApplicationAuthenticationFailedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.Set;

@ApplicationScoped
public class GoogleTokenVerificationService {

    private static final Set<String> ALLOWED_ISSUERS = Set.of(
            "accounts.google.com",
            "https://accounts.google.com"
    );

    private final GoogleTokenInfoClient googleTokenInfoClient;

    public GoogleTokenVerificationService(@RestClient GoogleTokenInfoClient googleTokenInfoClient) {
        this.googleTokenInfoClient = googleTokenInfoClient;
    }

    public GoogleTokenInfoResponseDto verifyIdToken(String idToken, String expectedAudience) {
        String normalizedIdToken = normalize(idToken);
        String normalizedExpectedAudience = normalize(expectedAudience);
        if (normalizedIdToken == null || normalizedExpectedAudience == null) {
            throw new ApplicationAuthenticationFailedException();
        }

        GoogleTokenInfoResponseDto googleTokenInfo;
        try {
            googleTokenInfo = this.googleTokenInfoClient.verifyIdToken(normalizedIdToken);
        } catch (WebApplicationException e) {
            throw new ApplicationAuthenticationFailedException();
        }

        if (!googleTokenInfo.isEmailVerified()
                || googleTokenInfo.getEmail() == null
                || googleTokenInfo.getEmail().isBlank()
                || googleTokenInfo.getAud() == null
                || !normalizedExpectedAudience.equals(googleTokenInfo.getAud().trim())
                || googleTokenInfo.getIss() == null
                || !ALLOWED_ISSUERS.contains(googleTokenInfo.getIss().trim())) {
            throw new ApplicationAuthenticationFailedException();
        }

        return googleTokenInfo;
    }

    private String normalize(String value) {
        return value != null ? value.trim() : null;
    }
}
