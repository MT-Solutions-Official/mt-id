package com.mtsolutions.domain.service;

import com.mtsolutions.application.exception.ApplicationAuthenticationFailedException;
import com.mtsolutions.application.utils.DateUtils;
import com.mtsolutions.domain.entity.UserRefreshToken;
import com.mtsolutions.domain.repository.UserRefreshTokenRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.time.LocalDateTime;

@ApplicationScoped
public class UserRefreshTokenService {

    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final TokenHashService tokenHashService;
    private final DateUtils dateUtils;

    public UserRefreshTokenService(UserRefreshTokenRepository userRefreshTokenRepository,
                                   TokenHashService tokenHashService,
                                   DateUtils dateUtils) {
        this.userRefreshTokenRepository = userRefreshTokenRepository;
        this.tokenHashService = tokenHashService;
        this.dateUtils = dateUtils;
    }

    public void registerRefreshToken(String tokenId, String userId, String appId, Duration expiration) {
        UserRefreshToken refreshToken = UserRefreshToken.builder()
                .tokenId(this.hash(tokenId))
                .userId(userId)
                .appId(appId)
                .createdAt(this.dateUtils.now())
                .expiresAt(this.dateUtils.now().plusSeconds(expiration.getSeconds()))
                .build();

        this.userRefreshTokenRepository.persist(refreshToken);
    }

    public UserRefreshToken validateActiveRefreshToken(String tokenId, String userId, String appId) {
        UserRefreshToken refreshToken = this.userRefreshTokenRepository.findByTokenId(this.hash(tokenId))
                .orElseThrow(ApplicationAuthenticationFailedException::new);

        LocalDateTime now = this.dateUtils.now();
        if (!userId.equals(refreshToken.getUserId())
                || !appId.equals(refreshToken.getAppId())
                || refreshToken.getRevokedAt() != null
                || refreshToken.getExpiresAt() == null
                || refreshToken.getExpiresAt().isBefore(now)) {
            throw new ApplicationAuthenticationFailedException();
        }

        return refreshToken;
    }

    public void revokeRefreshToken(String tokenId) {
        UserRefreshToken refreshToken = this.userRefreshTokenRepository.findByTokenId(this.hash(tokenId))
                .orElseThrow(ApplicationAuthenticationFailedException::new);

        this.markRevoked(refreshToken);
    }

    public void revokeAllForUser(String userId, String appId) {
        for (UserRefreshToken refreshToken : this.userRefreshTokenRepository.findActiveByUserIdAndAppId(userId, appId)) {
            this.markRevoked(refreshToken);
        }
    }

    public void revokeAllForApp(String appId) {
        for (UserRefreshToken refreshToken : this.userRefreshTokenRepository.findActiveByAppId(appId)) {
            this.markRevoked(refreshToken);
        }
    }

    private void markRevoked(UserRefreshToken refreshToken) {
        if (refreshToken.getRevokedAt() == null) {
            refreshToken.setRevokedAt(this.dateUtils.now());
            this.userRefreshTokenRepository.persistOrUpdate(refreshToken);
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
