package com.mtsolutions.application.client.kodepos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
public class KodePosAddressDto {

    @JsonProperty("code")
    private Integer zipCode;

    @JsonProperty("village")
    private String kelurahan;

    @JsonProperty("district")
    private String kecamatan;

    @JsonProperty("regency")
    private String city;

    @JsonProperty("province")
    private String province;

    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("longitude")
    private Double longitude;

    @JsonProperty("timezone")
    private String timezone;
}
