package com.mtsolutions.domain.model;

import com.mtsolutions.domain.constant.OwnerRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonRepresentation;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppOwnerMembership {

    @BsonId
    @BsonRepresentation(BsonType.OBJECT_ID)
    private String ownerId;
    private OwnerRole role;
}
