package com.mtsolutions.domain.model;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
public class Password {

    private String password;
    private String passwordResetToken;
    private LocalDateTime passwordResetTokenExpiry;
}
