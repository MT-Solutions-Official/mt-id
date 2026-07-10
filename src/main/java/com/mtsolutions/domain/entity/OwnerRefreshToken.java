package com.mtsolutions.domain.entity;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;
import org.bson.codecs.pojo.annotations.BsonId;

import java.time.LocalDateTime;

@MongoEntity(collection = "owner_refresh_tokens")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class OwnerRefreshToken {

    @BsonId
    private String tokenId;
    private String ownerId;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime revokedAt;
}
