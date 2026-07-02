package com.mtsolutions.entity;

import com.mtsolutions.model.*;
import com.mtsolutions.constant.MaritalStatus;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;
import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonRepresentation;

import java.time.LocalDateTime;
import java.util.List;

@MongoEntity(collection = "users")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
public class User {

    @BsonId
    @BsonRepresentation(BsonType.OBJECT_ID)
    private String userId;
    private String appId;
    private String name;
    private List<Email> emails;
    private Password password;
    private List<Phone> phones;
    private Document document;
    private MaritalStatus maritalStatus;
    private List<UserImage> images;
    private List<Address> addresses;
    private List<String> roleIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime disabledAt;
    private Boolean active;
}
