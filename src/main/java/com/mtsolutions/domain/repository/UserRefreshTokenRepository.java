package com.mtsolutions.domain.repository;

import com.mtsolutions.domain.entity.UserRefreshToken;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class UserRefreshTokenRepository implements PanacheMongoRepositoryBase<UserRefreshToken, String> {

    public Optional<UserRefreshToken> findByTokenId(String tokenId) {
        return findByIdOptional(tokenId);
    }
}
