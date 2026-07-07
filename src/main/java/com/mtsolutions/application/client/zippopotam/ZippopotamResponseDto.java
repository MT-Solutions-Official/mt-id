package com.mtsolutions.application.client.zippopotam;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
public class ZippopotamResponseDto {

    @JsonProperty("post code")
    private String postCode;

    @JsonProperty("country")
    private String country;

    @JsonProperty("country abbreviation")
    private String countryAbbreviation;

    @JsonProperty("places")
    private List<ZippopotamPlaceDto> places;

    public boolean isValid() {
        return places != null && !places.isEmpty();
    }
}
