package com.mtsolutions.application.client.kodepos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
public class KodePosResponseDto {

    @JsonProperty("statusCode")
    private Integer statusCode;

    @JsonProperty("code")
    private String code;

    @JsonProperty("data")
    private List<KodePosAddressDto> data;

    public boolean isValid() {
        return data != null && !data.isEmpty();
    }
}
