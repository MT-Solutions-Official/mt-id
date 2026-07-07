package com.mtsolutions.domain.entity;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;
import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonRepresentation;

@MongoEntity(collection = "owner_roles")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
public class OwnerRole {

    @BsonId
    @BsonRepresentation(BsonType.OBJECT_ID)
    private String ownerRoleId;
    private String roleName;
}
