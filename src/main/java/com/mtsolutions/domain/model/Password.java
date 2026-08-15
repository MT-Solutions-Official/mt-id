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
public class Password {

    @JsonIgnore
    private String password;
    @JsonIgnore
    private String passwordResetToken;
    @JsonIgnore
    private LocalDateTime passwordResetTokenExpiry;
}
