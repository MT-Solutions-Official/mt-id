package com.mtsolutions.application.resource.rest.examples;

public class ClientApplicationExamples {

    private ClientApplicationExamples() {
    }

    public static final String CREATE_CLIENT_APPLICATION = """
            {
                "name": "Example Client Application",
                "ownerId": "507f1f77bcf86cd799439011",
                "description": "This is an example client application.",
                "logoUrl": "https://cdn.example.com/logos/example.png",
                "emailSettings": {
                    "fromEmail": "no-reply@example.com",
                    "fromName": "Example App",
                    "replyTo": "support@example.com",
                    "supportEmail": "support@example.com",
                    "supportUrl": "https://example.com/support",
                    "verificationRedirectUrl": "https://example.com/verify",
                    "passwordResetRedirectUrl": "https://example.com/reset-password",
                    "loginUrl": "https://example.com/login"
                },
                "jwtExpirationInMinutes": 15,
                "refreshTokenExpirationInDays": 30,
                "allowedOrigins": ["https://example.com", "https://another-example.com"],
                "googleAudience": "1234567890-example.apps.googleusercontent.com",
                "requiredUserFields": ["NAME", "EMAIL", "PASSWORD"]
            }
            """;

    public static final String CLIENT_APPLICATION = """
            {
                "appId": "507f1f77bcf86cd799439011",
                "name": "Example Client Application",
                "description": "This is an example client application.",
                "logoUrl": "https://cdn.example.com/logos/example.png",
                "emailSettings": {
                    "fromEmail": "no-reply@example.com",
                    "fromName": "Example App",
                    "replyTo": "support@example.com",
                    "supportEmail": "support@example.com",
                    "supportUrl": "https://example.com/support",
                    "verificationRedirectUrl": "https://example.com/verify",
                    "passwordResetRedirectUrl": "https://example.com/reset-password",
                    "loginUrl": "https://example.com/login"
                },
                "apiKey": "exampleApiKey123456",
                "jwtExpirationInMinutes": 15,
                "refreshTokenExpirationInDays": 30,
                "allowedOrigins": ["https://example.com", "https://another-example.com"],
                "googleAudience": "1234567890-example.apps.googleusercontent.com",
                "requiredUserFields": ["NAME", "EMAIL", "PASSWORD"],
                "owners": [
                    {
                        "ownerId": "64a7f8b9c0e2a1b3d4f5e6a7",
                        "name": "Thiago Picanço",
                        "email": {
                            "email": "thiago@mtsolutions.com",
                            "verified": true
                        },
                        "role": "OWNER_WRITER",
                        "active": true
                    }
                ],
                "createdAt": "2024-06-01T12:00:00Z",
                "updatedAt": "2024-06-01T12:00:00Z",
                "active": true
            }
            """;

    public static final String CLIENT_APPLICATION_LIST = """
            [
                {
                    "appId": "507f1f77bcf86cd799439011",
                    "name": "Example Client Application",
                    "apiKey": "exampleApiKey123456",
                    "jwtExpirationInMinutes": 15,
                    "refreshTokenExpirationInDays": 30,
                    "allowedOrigins": ["https://example.com"],
                    "googleAudience": "1234567890-example.apps.googleusercontent.com",
                    "requiredUserFields": ["NAME", "EMAIL", "PASSWORD"],
                    "active": true
                }
            ]
            """;

    public static final String CLIENT_APPLICATION_CREATED = """
            {
                "appId": "507f1f77bcf86cd799439011",
                "name": "Example Client Application",
                "description": "This is an example client application.",
                "logoUrl": "https://cdn.example.com/logos/example.png",
                "emailSettings": {
                    "fromEmail": "no-reply@example.com",
                    "fromName": "Example App",
                    "replyTo": "support@example.com",
                    "supportEmail": "support@example.com",
                    "supportUrl": "https://example.com/support",
                    "verificationRedirectUrl": "https://example.com/verify",
                    "passwordResetRedirectUrl": "https://example.com/reset-password",
                    "loginUrl": "https://example.com/login"
                },
                "apiKey": "exampleApiKey123456",
                "apiSecret": "exampleApiSecret123456",
                "jwtExpirationInMinutes": 15,
                "refreshTokenExpirationInDays": 30,
                "allowedOrigins": ["https://example.com", "https://another-example.com"],
                "googleAudience": "1234567890-example.apps.googleusercontent.com",
                "requiredUserFields": ["NAME", "EMAIL", "PASSWORD"],
                "createdAt": "2024-06-01T12:00:00Z",
                "updatedAt": "2024-06-01T12:00:00Z",
                "active": true
            }
            """;

    public static final String ADD_OWNERS_TO_CLIENT_APPLICATION = """
            {
                "appId": "507f1f77bcf86cd799439011",
                "ownerIds": ["507f1f77bcf86cd799439012"],
                "emails": ["viewer@mtsolutions.com"],
                "role": "OWNER_VIEWER"
            }
            """;

    public static final String UPDATE_REQUIRED_USER_FIELDS = """
            {
                "appId": "507f1f77bcf86cd799439011",
                "requiredUserFields": ["NAME", "EMAIL", "PASSWORD", "PHONE"]
            }
            """;

    public static final String UPDATE_CLIENT_APPLICATION_SETTINGS = """
            {
                "appId": "507f1f77bcf86cd799439011",
                "name": "Example Client Application",
                "description": "Updated description.",
                "logoUrl": "https://cdn.example.com/logos/example.png",
                "emailSettings": {
                    "fromEmail": "no-reply@example.com",
                    "fromName": "Example App",
                    "replyTo": "support@example.com",
                    "supportEmail": "support@example.com",
                    "supportUrl": "https://example.com/support",
                    "verificationRedirectUrl": "https://example.com/verify",
                    "passwordResetRedirectUrl": "https://example.com/reset-password",
                    "loginUrl": "https://example.com/login"
                },
                "allowedOrigins": ["https://example.com"],
                "jwtExpirationInMinutes": 15,
                "refreshTokenExpirationInDays": 30,
                "googleAudience": "1234567890-example.apps.googleusercontent.com",
                "requiredUserFields": ["NAME", "EMAIL", "PASSWORD"]
            }
            """;

    public static final String CLIENT_APPLICATION_SECRET_ROTATED = """
            {
                "appId": "507f1f77bcf86cd799439011",
                "name": "Example Client Application",
                "description": "This is an example client application.",
                "logoUrl": "https://cdn.example.com/logos/example.png",
                "emailSettings": {
                    "fromEmail": "no-reply@example.com",
                    "fromName": "Example App",
                    "replyTo": "support@example.com",
                    "supportEmail": "support@example.com",
                    "supportUrl": "https://example.com/support",
                    "verificationRedirectUrl": "https://example.com/verify",
                    "passwordResetRedirectUrl": "https://example.com/reset-password",
                    "loginUrl": "https://example.com/login"
                },
                "apiKey": "exampleApiKey123456",
                "apiSecret": "exampleNewApiSecret123456",
                "jwtExpirationInMinutes": 15,
                "refreshTokenExpirationInDays": 30,
                "allowedOrigins": ["https://example.com", "https://another-example.com"],
                "requiredUserFields": ["NAME", "EMAIL", "PASSWORD"],
                "createdAt": "2024-06-01T12:00:00Z",
                "updatedAt": "2024-06-01T12:30:00Z",
                "active": true
            }
            """;
}
