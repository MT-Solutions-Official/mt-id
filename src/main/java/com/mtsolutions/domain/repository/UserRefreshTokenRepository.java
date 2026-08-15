package com.mtsolutions.domain.repository;

import com.mtsolutions.domain.entity.UserRefreshToken;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserRefreshTokenRepository implements PanacheMongoRepositoryBase<UserRefreshToken, String> {

    public Optional<UserRefreshToken> findByTokenId(String tokenId) {
        return findByIdOptional(tokenId);
    }

    public List<UserRefreshToken> findActiveByUserIdAndAppId(String userId, String appId) {
        return find("{userId: ?1, appId: ?2, revokedAt: null}", userId, appId).list();
    }
}
