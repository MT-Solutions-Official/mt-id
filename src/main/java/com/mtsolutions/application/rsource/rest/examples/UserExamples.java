package com.mtsolutions.application.rsource.rest.examples;

public class UserExamples {

    public static final String CREATE_USER = """
            {
              "appId": "507f1f77bcf86cd799439011",
              "name": "John Doe",
              "username": "john.doe",
              "email": ["john.doe@example.com"],
              "password": "StrongPassword123!",
              "phones": [
                {
                  "phoneNumber": "+5511999999999"
                }
              ],
              "document": {
                "cpf": "12345678900"
              },
              "maritalStatus": "SINGLE",
              "roles": ["admin"]
            }
            """;

    public static final String USER_CREATED = """
            {
              "userId": "64a7f8b9c0e2a1b3d4f5e6a8",
              "appId": "507f1f77bcf86cd799439011",
              "name": "John Doe",
              "username": "john.doe",
              "emails": [
                {
                  "email": "john.doe@example.com",
                  "verificationToken": null,
                  "verificationTokenExpiry": null,
                  "verified": false
                }
              ],
              "password": {
                "password": "$2a$10$...",
                "passwordResetToken": null,
                "passwordResetTokenExpiry": null
              },
              "phones": [
                {
                  "phoneNumber": "+5511999999999",
                  "verificationToken": null,
                  "verificationTokenExpiry": null,
                  "verified": false
                }
              ],
              "document": {
                "cpf": "12345678900",
                "rg": null,
                "cnpj": null,
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
              "maritalStatus": "SINGLE",
              "images": [],
              "addresses": [],
              "roleIds": ["admin"],
              "createdAt": "2026-07-03T18:10:00.000Z",
              "updatedAt": "2026-07-03T18:10:00.000Z",
              "disabledAt": null,
              "active": true
            }
            """;

    public static final String ATTACH_ADDRESS_TO_USER_BR = """
            {
              "mode": "AUTO",
              "country": "BR",
              "zipCode": "28994666",
              "number": "123",
              "complement": "Apto 1"
            }
            """;

    public static final String ATTACH_ADDRESS_TO_USER_ID = """
            {
              "mode": "AUTO",
              "country": "ID",
              "zipCode": "10110",
              "street": "Jalan Thamrin",
              "number": "10",
              "rt": "001",
              "rw": "002",
              "complement": "Blok A"
            }
            """;

    public static final String ATTACH_ADDRESS_TO_USER_US = """
            {
              "mode": "AUTO",
              "country": "US",
              "zipCode": "90210",
              "street": "N Canon Dr",
              "number": "200"
            }
            """;

    public static final String ATTACH_ADDRESS_TO_USER_PT = """
            {
              "mode": "AUTO",
              "country": "PT",
              "zipCode": "1000-001",
              "street": "Rua Augusta",
              "number": "120"
            }
            """;

    public static final String ATTACH_ADDRESS_TO_USER_BR_MANUAL = """
            {
              "mode": "MANUAL",
              "country": "BR",
              "zipCode": "28994666",
              "street": "Rua Moacir Picanço",
              "number": "123",
              "complement": "Apto 1",
              "neighborhood": "Bacaxá (Bacaxá)",
              "city": "Saquarema",
              "state": "RJ"
            }
            """;

    public static final String USER_WITH_ADDRESS = """
            {
              "userId": "64a7f8b9c0e2a1b3d4f5e6a8",
              "appId": "507f1f77bcf86cd799439011",
              "name": "John Doe",
              "username": "john.doe",
              "emails": [
                {
                  "email": "john.doe@example.com",
                  "verificationToken": null,
                  "verificationTokenExpiry": null,
                  "verified": false
                }
              ],
              "phones": [
                {
                  "phoneNumber": "+5511999999999",
                  "verificationToken": null,
                  "verificationTokenExpiry": null,
                  "verified": false
                }
              ],
              "addresses": [
                {
                  "country": "BR",
                  "zipCode": "28994-666",
                  "street": "Rua Moacir Picanço",
                  "number": "123",
                  "complement": "Apto 1",
                  "neighborhood": "Bacaxá (Bacaxá)",
                  "city": "Saquarema",
                  "state": "RJ"
                }
              ],
              "roleIds": ["admin"],
              "createdAt": "2026-07-03T18:10:00.000Z",
              "updatedAt": "2026-07-03T18:12:00.000Z",
              "disabledAt": null,
              "active": true
            }
            """;
}
