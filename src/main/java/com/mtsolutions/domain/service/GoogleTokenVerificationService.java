package com.mtsolutions.domain.service;

import com.mtsolutions.application.client.google.GoogleTokenInfoResponseDto;
import com.mtsolutions.application.exception.ApplicationAuthenticationFailedException;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;

import java.util.List;
import java.util.Set;

@ApplicationScoped
public class GoogleTokenVerificationService {

    private static final Set<String> ALLOWED_ISSUERS = Set.of(
            "accounts.google.com",
            "https://accounts.google.com"
    );

    private final HttpsJwks httpsJwks;

    public GoogleTokenVerificationService(
            @ConfigProperty(name = "app.mt.id.google.jwks-url", defaultValue = "https://www.googleapis.com/oauth2/v3/certs")
            String jwksUrl) {
        this.httpsJwks = new HttpsJwks(jwksUrl);
        this.httpsJwks.setDefaultCacheDuration(3600);
    }

    public GoogleTokenInfoResponseDto verifyIdToken(String idToken, String expectedAudience) {
        return this.verifyIdToken(idToken, expectedAudience, null);
    }

    public GoogleTokenInfoResponseDto verifyIdToken(String idToken, String expectedAudience, String expectedNonce) {
        String normalizedIdToken = normalize(idToken);
        String normalizedAudience = normalize(expectedAudience);
        if (normalizedIdToken == null || normalizedAudience == null) {
            throw new ApplicationAuthenticationFailedException();
        }

        JwtClaims claims;
        try {
            JwtConsumer consumer = new JwtConsumerBuilder()
                    .setRequireExpirationTime()
                    .setAllowedClockSkewInSeconds(60)
                    .setRequireSubject()
                    .setExpectedAudience(normalizedAudience)
                    .setExpectedIssuers(true, ALLOWED_ISSUERS.toArray(String[]::new))
                    .setVerificationKeyResolver(new HttpsJwksVerificationKeyResolver(this.httpsJwks))
                    .build();
            claims = consumer.processToClaims(normalizedIdToken);
        } catch (InvalidJwtException e) {
            throw new ApplicationAuthenticationFailedException();
        }

        try {
            String email = claims.getStringClaimValue("email");
            if (email == null || email.isBlank() || !isEmailVerified(claims)) {
                throw new ApplicationAuthenticationFailedException();
            }

            String nonce = claims.getStringClaimValue("nonce");
            String normalizedExpectedNonce = normalize(expectedNonce);
            if (normalizedExpectedNonce != null && !normalizedExpectedNonce.equals(normalize(nonce))) {
                throw new ApplicationAuthenticationFailedException();
            }

            return GoogleTokenInfoResponseDto.builder()
                    .email(email)
                    .emailVerified("true")
                    .aud(firstAudience(claims))
                    .iss(claims.getIssuer())
                    .name(claims.getStringClaimValue("name"))
                    .sub(claims.getSubject())
                    .picture(claims.getStringClaimValue("picture"))
                    .hd(claims.getStringClaimValue("hd"))
                    .build();
        } catch (MalformedClaimException e) {
            throw new ApplicationAuthenticationFailedException();
        }
    }

    private boolean isEmailVerified(JwtClaims claims) throws MalformedClaimException {
        Object value = claims.getClaimValue("email_verified");
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof String stringValue) {
            return Boolean.parseBoolean(stringValue);
        }
        return false;
    }

    private String firstAudience(JwtClaims claims) throws MalformedClaimException {
        List<String> audiences = claims.getAudience();
        if (audiences == null || audiences.isEmpty()) {
            return null;
        }
        return audiences.getFirst();
    }

    private String normalize(String value) {
        return value != null && !value.isBlank() ? value.trim() : null;
    }
}
