package com.mtsolutions.domain.entity;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;
import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonRepresentation;

import java.time.LocalDateTime;
import java.util.List;

@MongoEntity(collection = "applications")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
public class ClientApplication {

    @BsonId
    @BsonRepresentation(BsonType.OBJECT_ID)
    private String appId;
    private String name;
    private String description;

    private String apiKey;
    private String apiSecret;

    private Integer jwtExpirationInMinutes;
    private Integer refreshTokenExpirationInDays;

    private List<String> allowedOrigins;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean active;
}