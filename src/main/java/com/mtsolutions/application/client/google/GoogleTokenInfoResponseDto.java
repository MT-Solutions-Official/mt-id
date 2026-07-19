package com.mtsolutions.application.client.google;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class GoogleTokenInfoResponseDto {

    private String email;

    @JsonProperty("email_verified")
    private String emailVerified;

    private String aud;
    private String iss;
    private String name;
    private String sub;
    private String picture;
    private String hd;

    public boolean isEmailVerified() {
        return Boolean.parseBoolean(this.emailVerified);
    }
}
