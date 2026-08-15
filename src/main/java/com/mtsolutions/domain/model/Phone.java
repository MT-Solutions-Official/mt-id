package com.mtsolutions.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Phone {

    private String phoneNumber;
    @JsonIgnore
    private String verificationToken;
    @JsonIgnore
    private LocalDateTime verificationTokenExpiry;
    private Boolean verified;
}
