package com.mtsolutions.domain.entity;

import com.mtsolutions.domain.model.*;
import com.mtsolutions.domain.constant.MaritalStatus;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;
import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonRepresentation;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private String username;
    private String primaryEmail;
    private List<Email> emails = new ArrayList<>();
    private Password password;
    private List<Phone> phones = new ArrayList<>();
    private Document document;
    private MaritalStatus maritalStatus;
    private List<UserImage> images = new ArrayList<>();
    private List<Address> addresses = new ArrayList<>();
    private List<String> roleIds = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime disabledAt;
    private Boolean active;
}
