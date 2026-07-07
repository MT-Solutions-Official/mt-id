package com.mtsolutions.domain.entity;

import com.mtsolutions.domain.constant.OwnerRole;
import com.mtsolutions.domain.model.Document;
import com.mtsolutions.domain.model.Email;
import com.mtsolutions.domain.model.Password;
import com.mtsolutions.domain.model.Phone;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;
import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonRepresentation;

import java.time.LocalDateTime;

@MongoEntity(collection = "application_owners")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
public class ApplicationOwner {

    @BsonId
    @BsonRepresentation(BsonType.OBJECT_ID)
    private String ownerId;
    private String name;
    private Email email;
    private Phone phone;
    private Document document;
    private Password password;
    private OwnerRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime disabledAt;
    private Boolean active;
}
