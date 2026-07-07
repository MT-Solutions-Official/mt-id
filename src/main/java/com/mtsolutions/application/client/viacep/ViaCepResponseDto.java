package com.mtsolutions.application.client.viacep;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
public class ViaCepResponseDto {

    @JsonProperty("cep") private String zipCode;
    @JsonProperty("logradouro") private String street;
    @JsonProperty("bairro") private String neighborhood;
    @JsonProperty("localidade") private String city;
    @JsonProperty("uf") private String state;
    @JsonProperty("erro") private Boolean error;

    public boolean isValid() {
        return error == null || !error;
    }

}
