package com.mtsolutions.application.resource.rest.examples;

public class OwnerExamples {

    public static final String CREATE_OWNER = """
            {
              "name": "Thiago Picanço",
              "email": "thiago@mtsolutions.com",
              "password": "StrongPassword123!",
              "phoneNumber": "+5521999999999",
              "document": {
                "cpf": "12345678900",
                "cnpj": "12345678000190"
              }
            }
            """;

    public static final String OWNER_CREATED = """
            {
              "ownerId": "64a7f8b9c0e2a1b3d4f5e6a7",
              "name": "Thiago Picanço",
              "email": {
                "email": "thiago@mtsolutions.com",
                "verificationToken": null,
                "verificationTokenExpiry": null,
                "verified": false
              },
              "phone": {
                "phoneNumber": "+5521999999999",
                "verificationToken": null,
                "verificationTokenExpiry": null,
                "verified": false
              },
              "document": {
                "cpf": "12345678900",
                "rg": null,
                "cnpj": "12345678000190",
                "cnh": null,
                "nik": null,
                "npwp": null,
                "sim": null,
                "ssn": null,
                "ein": null,
                "usDriverLicense": null,
                "nif": null,
                "niss": null,
                "cc": null,
                "passport": null
              },
              "createdAt": "2026-07-02T14:12:38.000Z",
              "updatedAt": "2026-07-02T14:12:38.000Z",
              "active": true
            }
            """;
}