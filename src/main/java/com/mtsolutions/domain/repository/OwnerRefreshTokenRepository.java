package com.mtsolutions.domain.repository;

import com.mtsolutions.domain.entity.OwnerRefreshToken;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class OwnerRefreshTokenRepository implements PanacheMongoRepositoryBase<OwnerRefreshToken, String> {

    public Optional<OwnerRefreshToken> findByTokenId(String tokenId) {
        return findByIdOptional(tokenId);
    }
}
