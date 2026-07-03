package com.mtsolutions.domain.constant;

import lombok.Getter;

@Getter
public enum Origin {

    MT_ID("MT ID"),
    VIACEP("ViaCEP"),
    KODEPOS("KodePos"),
    ZIPPOPOTAM("Zippopotam");

    private final String displayName;

    Origin(String displayName) {
        this.displayName = displayName;
    }
}
