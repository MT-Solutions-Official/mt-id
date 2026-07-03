package com.mtsolutions.domain.dto;

public record CreateDocumentRequestDto(
        String cpf,
        String rg,
        String cnpj,
        String cnh,
        String nik,
        String npwp,
        String sim,
        String ssn,
        String ein,
        String usDriverLicense,
        String nif,
        String niss,
        String cc,
        String passport
) {
}
