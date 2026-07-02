package com.mtsolutions.entity;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;
import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonRepresentation;

@MongoEntity(collection = "user_roles")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
public class UserRole {

    @BsonId
    @BsonRepresentation(BsonType.OBJECT_ID)
    private String userRoleId;
    private String appId;
    private String roleName;
}
