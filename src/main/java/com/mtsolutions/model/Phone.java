package com.mtsolutions.model;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
public class Phone {

    private String phoneNumber;
    private String verificationToken;
    private LocalDateTime verificationTokenExpiry;
    private Boolean verified;
}
