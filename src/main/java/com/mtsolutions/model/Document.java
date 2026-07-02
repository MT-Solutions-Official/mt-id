package com.mtsolutions.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
public class Document {

    // BRAZIL
    private String cpf;
    private String rg;
    private String cnpj;
    private String cnh;

    // INDONESIA
    private String nik;  // Identidade Nacional (KTP)
    private String npwp; // Identificação Fiscal
    private String sim;  // Carteira de Motorista

    // UNITED STATES
    private String ssn;  // Social Security Number
    private String ein;  // Employer Identification Number (Empresas)
    private String usDriverLicense;

    // PORTUGAL
    private String nif;  // Identificação Fiscal
    private String niss; // Segurança Social
    private String cc;   // Cartão de Cidadão

    // GENERAL
    private String passport;

}