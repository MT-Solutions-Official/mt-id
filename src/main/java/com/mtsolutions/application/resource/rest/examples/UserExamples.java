package com.mtsolutions.application.resource.rest.examples;

public class UserExamples {

    private UserExamples() {
    }

    public static final String CREATE_USER = """
            {
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
                  "primary": true,
                  "verified": false
                }
              ],
              "phones": [
                {
                  "phoneNumber": "+5511999999999",
                  "verified": false
                }
              ],
              "document": {
                "cpf": "12345678900"
              },
              "maritalStatus": "SINGLE",
              "images": [],
              "addresses": [],
              "roleIds": ["admin"],
              "createdAt": "2026-07-03T18:10:00.000Z",
              "updatedAt": "2026-07-03T18:10:00.000Z",
              "active": true
            }
            """;

    public static final String SEND_EMAIL_VERIFICATION = """
            {
              "email": "john.doe@example.com"
            }
            """;

    public static final String FORGOT_PASSWORD = """
            {
              "email": "john.doe@example.com",
              "appId": "507f1f77bcf86cd799439011"
            }
            """;

    public static final String RESET_PASSWORD = """
            {
              "token": "f8b9d13f-c980-4efb-a7ef-c7ecf4cf4d10",
              "newPassword": "StrongPassword123!"
            }
            """;

    public static final String ATTACH_ADDRESS_TO_USER_BR = """
            {
              "country": "BR",
              "zipCode": "28994666",
              "street": "Rua Moacir Picanço",
              "number": "123",
              "complement": "Apto 1",
              "city": "Saquarema",
              "state": "RJ",
              "neighborhood": "Bacaxá (Bacaxá)"
            }
            """;

    public static final String ATTACH_ADDRESS_TO_USER_ID = """
            {
              "country": "ID",
              "zipCode": "10110",
              "street": "Jalan Thamrin",
              "number": "10",
              "city": "Jakarta",
              "state": "DKI Jakarta",
              "rt": "001",
              "rw": "002",
              "complement": "Blok A",
              "kelurahan": "Gondangdia",
              "kecamatan": "Menteng"
            }
            """;

    public static final String ATTACH_ADDRESS_TO_USER_US = """
            {
              "country": "US",
              "zipCode": "90210",
              "street": "N Canon Dr",
              "number": "200",
              "city": "Beverly Hills",
              "state": "CA"
            }
            """;

    public static final String ATTACH_ADDRESS_TO_USER_PT = """
            {
              "country": "PT",
              "zipCode": "1000-001",
              "street": "Rua Augusta",
              "number": "123",
              "city": "Lisboa",
              "state": "Lisboa",
              "neighborhood": "Santa Maria Maior"
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
                  "primary": true,
                  "verified": false
                }
              ],
              "phones": [
                {
                  "phoneNumber": "+5511999999999",
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

    public static final String USER_WITH_IMAGE = """
            {
              "userId": "64a7f8b9c0e2a1b3d4f5e6a8",
              "appId": "507f1f77bcf86cd799439011",
              "name": "John Doe",
              "username": "john.doe",
              "images": [
                {
                  "imageUrl": "https://res.cloudinary.com/demo/image/upload/v1234567890/mt-id/users/pictures/507f1f77bcf86cd799439011/profile/uuid.jpg",
                  "imageType": "PROFILE",
                  "fileName": "profile.jpg",
                  "sizeInBytes": 128456,
                  "verified": false,
                  "uploadedAt": "2026-07-09T16:40:00.000"
                }
              ],
              "createdAt": "2026-07-03T18:10:00.000Z",
              "updatedAt": "2026-07-09T16:40:00.000Z",
              "disabledAt": null,
              "active": true
            }
            """;
}
