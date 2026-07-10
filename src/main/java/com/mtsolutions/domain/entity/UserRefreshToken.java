package com.mtsolutions.domain.entity;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;
import org.bson.codecs.pojo.annotations.BsonId;

import java.time.LocalDateTime;

@MongoEntity(collection = "user_refresh_tokens")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class UserRefreshToken {

    @BsonId
    private String tokenId;
    private String userId;
    private String appId;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime revokedAt;
}
