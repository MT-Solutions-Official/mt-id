package com.mtsolutions.domain.repository;

import com.mtsolutions.domain.entity.OwnerRefreshToken;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class OwnerRefreshTokenRepository implements PanacheMongoRepositoryBase<OwnerRefreshToken, String> {

    public Optional<OwnerRefreshToken> findByTokenId(String tokenId) {
        return findByIdOptional(tokenId);
    }

    public List<OwnerRefreshToken> findActiveByOwnerId(String ownerId) {
        return find("{ownerId: ?1, revokedAt: null}", ownerId).list();
    }
}
