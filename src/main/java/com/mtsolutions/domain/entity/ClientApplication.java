package com.mtsolutions.domain.entity;

import com.mtsolutions.domain.constant.UserRequiredField;
import com.mtsolutions.domain.model.EmailSettings;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;
import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonRepresentation;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Builder.Default
    private List<Owner> owners = new ArrayList<>();
    private String name;
    private String description;
    private String logoUrl;
    private EmailSettings emailSettings;

    private String apiKey;
    private String apiSecret;

    private Integer jwtExpirationInMinutes;
    private Integer refreshTokenExpirationInDays;

    private List<String> allowedOrigins;
    @Builder.Default
    private List<UserRequiredField> requiredUserFields = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean active;
}