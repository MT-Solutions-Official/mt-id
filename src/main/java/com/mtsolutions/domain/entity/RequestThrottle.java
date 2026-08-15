package com.mtsolutions.domain.entity;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;
import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonRepresentation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@MongoEntity(collection = "request_throttles")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class RequestThrottle {

    @BsonId
    @BsonRepresentation(BsonType.OBJECT_ID)
    private String throttleId;
    private String key;
    @Builder.Default
    private List<LocalDateTime> attempts = new ArrayList<>();
    private LocalDateTime lastAttemptAt;
    private LocalDateTime updatedAt;
}
