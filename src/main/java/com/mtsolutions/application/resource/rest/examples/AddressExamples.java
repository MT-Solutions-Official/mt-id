package com.mtsolutions.application.resource.rest.examples;

public class AddressExamples {

    // ── Brazil ────────────────────────────────────────────────────────────────

    public static final String BR_RESPONSE =
            """
            {
              "zipCode": "01310-100",
              "street": "Avenida Paulista",
              "number": "1000",
              "complement": "Apto 42",
              "neighborhood": "Bela Vista",
              "city": "São Paulo",
              "state": "SP"
            }
            """;

    // ── Indonesia ─────────────────────────────────────────────────────────────

    public static final String ID_RESPONSE =
            """
            {
              "zipCode": "10110",
              "street": "Jl. Medan Merdeka Barat",
              "number": "12",
              "complement": "Lantai 2",
              "rt": "001",
              "rw": "003",
              "kelurahan": "Gambir",
              "kecamatan": "Gambir",
              "city": "Administrasi Jakarta Pusat",
              "state": "DKI Jakarta"
            }
            """;

    // ── Portugal ──────────────────────────────────────────────────────────────

    public static final String PT_RESPONSE =
            """
            {
              "zipCode": "1000-001",
              "street": "Rua Augusta",
              "number": "123",
              "complement": "2º Andar",
              "city": "Lisboa",
              "state": "Lisboa"
            }
            """;

    // ── United States ─────────────────────────────────────────────────────────

    public static final String US_RESPONSE =
            """
            {
              "zipCode": "90210",
              "street": "Rodeo Drive",
              "number": "450",
              "complement": "Suite 100",
              "city": "Beverly Hills",
              "state": "California"
            }
            """;

    private AddressExamples() {}
}
