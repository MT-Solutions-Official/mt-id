package com.mtsolutions.domain.entity;

import com.mtsolutions.domain.model.Address;
import com.mtsolutions.domain.model.Document;
import com.mtsolutions.domain.model.Email;
import com.mtsolutions.domain.model.Password;
import com.mtsolutions.domain.model.Phone;
import com.mtsolutions.domain.model.UserImage;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;
import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonRepresentation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@MongoEntity(collection = "owners")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
public class Owner {

    @BsonId
    @BsonRepresentation(BsonType.OBJECT_ID)
    private String ownerId;
    private String name;
    private Email email;
    private Phone phone;
    private Document document;
    private Password password;
    @Builder.Default
    private List<UserImage> images = new ArrayList<>();
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime disabledAt;
    private Boolean active;
}
