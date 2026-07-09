package com.mtsolutions.domain.constant;

import lombok.Getter;

@Getter
public enum ImageType {
    PROFILE("Profile Picture"),
    DOCUMENT_FRONT("Document Front"),
    DOCUMENT_BACK("Document Back"),
    SELFIE_KYC("KYC Selfie"), // Para reconhecimento facial/prova de vida
    PROOF_OF_ADDRESS("Proof of Address"),
    OTHER("Other");

    private final String displayName;

    ImageType(String displayName) {
        this.displayName = displayName;
    }
}