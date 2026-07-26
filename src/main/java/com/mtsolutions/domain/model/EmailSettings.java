package com.mtsolutions.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailSettings {

    private String fromEmail;
    private String fromName;
    private String replyTo;
    private String supportEmail;
    private String supportUrl;
    private String verificationRedirectUrl;
    private String passwordResetRedirectUrl;
}

