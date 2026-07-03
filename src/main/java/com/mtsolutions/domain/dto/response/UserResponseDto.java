package com.mtsolutions.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mtsolutions.domain.constant.MaritalStatus;
import com.mtsolutions.domain.entity.User;
import com.mtsolutions.domain.model.Address;
import com.mtsolutions.domain.model.Document;
import com.mtsolutions.domain.model.Email;
import com.mtsolutions.domain.model.Password;
import com.mtsolutions.domain.model.Phone;
import com.mtsolutions.domain.model.UserImage;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponseDto(
        String userId,
        String appId,
        String name,
        String username,
        List<Email> emails,
        Password password,
        List<Phone> phones,
        Document document,
        MaritalStatus maritalStatus,
        List<UserImage> images,
        List<Address> addresses,
        List<String> roleIds,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime disabledAt,
        Boolean active
) {

    public UserResponseDto(User user) {
        this(
                user.getUserId(),
                user.getAppId(),
                user.getName(),
                user.getUsername(),
                user.getEmails(),
                user.getPassword(),
                user.getPhones(),
                user.getDocument(),
                user.getMaritalStatus(),
                user.getImages(),
                user.getAddresses(),
                user.getRoleIds(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getDisabledAt(),
                user.getActive()
        );
    }
}
