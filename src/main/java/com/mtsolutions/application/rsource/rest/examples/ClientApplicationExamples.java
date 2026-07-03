package com.mtsolutions.application.rsource.rest.examples;

public class ClientApplicationExamples {

    public static final String CREATE_CLIENT_APPLICATION = """
            {
                "name": "Example Client Application",
                "ownerId": "507f1f77bcf86cd799439011",
                "description": "This is an example client application.",
                "jwtExpirationInMinutes": 60,
                "refreshTokenExpirationInDays": 30,
                "allowedOrigins": ["https://example.com", "https://another-example.com"],
                "requiredUserFields": ["NAME", "EMAIL", "PASSWORD"]
            }
            """;

    public static final String CLIENT_APPLICATION_CREATED = """
            {
                "appId": "507f1f77bcf86cd799439011",
                "name": "Example Client Application",
                "description": "This is an example client application.",
                "apiKey": "exampleApiKey123456",
                "apiSecret": "exampleApiSecret123456",
                "jwtExpirationInMinutes": 60,
                "refreshTokenExpirationInDays": 30,
                "allowedOrigins": ["https://example.com", "https://another-example.com"],
                "requiredUserFields": ["NAME", "EMAIL", "PASSWORD"],
                "createdAt": "2024-06-01T12:00:00Z",
                "updatedAt": "2024-06-01T12:00:00Z",
                "active": true
            }
            """;

    public static final String ADD_OWNERS_TO_CLIENT_APPLICATION = """
            {
                "appId": "507f1f77bcf86cd799439011",
                "ownerIds": ["507f1f77bcf86cd799439012", "507f1f77bcf86cd799439013"]
            }
            """;

    public static final String UPDATE_REQUIRED_USER_FIELDS = """
            {
                "appId": "507f1f77bcf86cd799439011",
                "requiredUserFields": ["NAME", "EMAIL", "PASSWORD", "PHONE"]
            }
            """;
}
