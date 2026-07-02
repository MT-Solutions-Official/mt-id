package com.mtsolutions.domain.model;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
public class Email {

    private String email;
    private String verificationToken;
    private LocalDateTime verificationTokenExpiry;
    private Boolean verified;
}
