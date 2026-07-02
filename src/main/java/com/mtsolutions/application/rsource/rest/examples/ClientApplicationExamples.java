package com.mtsolutions.application.rsource.rest.examples;

public class ClientApplicationExamples {

    public static final String CREATE_CLIENT_APPLICATION = """
            {
                "name": "Example Client Application",
                "description": "This is an example client application.",
                "jwtExpirationInMinutes": 60,
                "refreshTokenExpirationInDays": 30,
                "allowedOrigins": ["https://example.com", "https://another-example.com"]
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
                "createdAt": "2024-06-01T12:00:00Z",
                "updatedAt": "2024-06-01T12:00:00Z",
                "active": true
            }
            """;
}
