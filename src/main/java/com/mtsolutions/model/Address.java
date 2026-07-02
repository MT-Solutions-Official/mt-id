package com.mtsolutions.model;

import com.mtsolutions.constant.Country;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
public class Address {

    private Country country;

    // Universal address fields
    private String zipCode;      // CEP, Kode Pos, ZIP Code, Código Postal
    private String street;       // Logradouro, Jalan, Street Address, Arruamento
    private String number;       // Número, House Number, Número de Porta
    private String complement;   // Complemento, Apt, Suite, Andar
    private String city;         // Cidade, Kota/Kabupaten, Concelho
    private String state;        // Estado (UF), Provinsi, State, Distrito

    // Specific to Brazil, USA, and Portugal
    private String neighborhood; // Bairro (BR), Freguesia (PT)

    // Specific to Indonesia
    private String rt;           // Rukun Tetangga (Divisão de vizinhança)
    private String rw;           // Rukun Warga (Divisão de vizinhança)
    private String kelurahan;    // Vila / Subdistrito
    private String kecamatan;    // Distrito
}