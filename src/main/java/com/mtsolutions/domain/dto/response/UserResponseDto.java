package com.mtsolutions.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mtsolutions.domain.constant.MaritalStatus;
import com.mtsolutions.domain.entity.User;
import com.mtsolutions.domain.model.Address;
import com.mtsolutions.domain.model.Document;
import com.mtsolutions.domain.model.Email;
import com.mtsolutions.domain.model.Phone;
import com.mtsolutions.domain.model.UserImage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponseDto(
        String userId,
        String appId,
        String name,
        String username,
        List<EmailResponseDto> emails,
        List<PhoneResponseDto> phones,
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
                mapEmails(user.getEmails()),
                mapPhones(user.getPhones()),
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

    private static List<EmailResponseDto> mapEmails(List<Email> emails) {
        if (emails == null) {
            return null;
        }
        return emails.stream()
                .filter(Objects::nonNull)
                .map(EmailResponseDto::new)
                .toList();
    }

    private static List<PhoneResponseDto> mapPhones(List<Phone> phones) {
        if (phones == null) {
            return null;
        }
        return phones.stream()
                .filter(Objects::nonNull)
                .map(PhoneResponseDto::new)
                .toList();
    }
}
