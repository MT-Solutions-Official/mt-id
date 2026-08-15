package com.mtsolutions.domain.service;

import com.mtsolutions.application.exception.ApplicationAuthenticationFailedException;
import com.mtsolutions.application.utils.DateUtils;
import com.mtsolutions.domain.entity.OwnerRefreshToken;
import com.mtsolutions.domain.repository.OwnerRefreshTokenRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.time.LocalDateTime;

@ApplicationScoped
public class OwnerRefreshTokenService {

    private final OwnerRefreshTokenRepository ownerRefreshTokenRepository;
    private final TokenHashService tokenHashService;
    private final DateUtils dateUtils;

    public OwnerRefreshTokenService(OwnerRefreshTokenRepository ownerRefreshTokenRepository,
                                    TokenHashService tokenHashService,
                                    DateUtils dateUtils) {
        this.ownerRefreshTokenRepository = ownerRefreshTokenRepository;
        this.tokenHashService = tokenHashService;
        this.dateUtils = dateUtils;
    }

    public void registerRefreshToken(String tokenId, String ownerId, Duration expiration) {
        OwnerRefreshToken refreshToken = OwnerRefreshToken.builder()
                .tokenId(this.hash(tokenId))
                .ownerId(ownerId)
                .createdAt(this.dateUtils.now())
                .expiresAt(this.dateUtils.now().plusSeconds(expiration.getSeconds()))
                .build();

        this.ownerRefreshTokenRepository.persist(refreshToken);
    }

    public OwnerRefreshToken validateActiveRefreshToken(String tokenId, String ownerId) {
        OwnerRefreshToken refreshToken = this.ownerRefreshTokenRepository.findByTokenId(this.hash(tokenId))
                .orElseThrow(ApplicationAuthenticationFailedException::new);

        LocalDateTime now = this.dateUtils.now();
        if (!ownerId.equals(refreshToken.getOwnerId())
                || refreshToken.getRevokedAt() != null
                || refreshToken.getExpiresAt() == null
                || refreshToken.getExpiresAt().isBefore(now)) {
            throw new ApplicationAuthenticationFailedException();
        }

        return refreshToken;
    }

    public void revokeRefreshToken(String tokenId) {
        OwnerRefreshToken refreshToken = this.ownerRefreshTokenRepository.findByTokenId(this.hash(tokenId))
                .orElseThrow(ApplicationAuthenticationFailedException::new);

        this.markRevoked(refreshToken);
    }

    public void revokeAllForOwner(String ownerId) {
        for (OwnerRefreshToken refreshToken : this.ownerRefreshTokenRepository.findActiveByOwnerId(ownerId)) {
            this.markRevoked(refreshToken);
        }
    }

    private void markRevoked(OwnerRefreshToken refreshToken) {
        if (refreshToken.getRevokedAt() == null) {
            refreshToken.setRevokedAt(this.dateUtils.now());
            this.ownerRefreshTokenRepository.persistOrUpdate(refreshToken);
        }
    }

    private String hash(String rawTokenId) {
        String hashed = this.tokenHashService.hash(rawTokenId);
        if (hashed == null) {
            throw new ApplicationAuthenticationFailedException();
        }
        return hashed;
    }
}
