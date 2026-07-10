package com.mtsolutions.domain.dto.request;

import com.mtsolutions.domain.constant.MaritalStatus;
import com.mtsolutions.domain.model.Phone;

import java.util.List;

public record CreateUserRequestDto (
        String name,
        String username,
        List<String> email,
        String password,
        List<Phone> phones,
        CreateDocumentRequestDto document,
        MaritalStatus maritalStatus,
        List<String> roles

) {
}
