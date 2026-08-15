package com.mtsolutions.application.resource.rest.examples;

public class ApplicationAuthExamples {

    public static final String OWNER_TOKEN_REQUEST = """
            {
              "email": "owner@company.com",
              "password": "owner-password"
            }
            """;

    public static final String OWNER_TOKEN_RESPONSE = """
            {
              "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
              "refreshToken": "eyJhbGciOiJSUzI1NiJ9...",
              "tokenType": "Bearer",
              "expiresIn": 900,
              "refreshTokenExpiresIn": 2592000
            }
            """;

    public static final String APP_TOKEN_RESPONSE = """
            {
              "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
              "tokenType": "Bearer",
              "expiresIn": 3600
            }
            """;

    public static final String USER_TOKEN_REQUEST = """
            {
              "email": "user@example.com",
              "password": "user-password",
              "appId": "507f1f77bcf86cd799439011"
            }
            """;

    public static final String GOOGLE_TOKEN_REQUEST = """
            {
              "idToken": "google-id-token"
            }
            """;

    public static final String USER_GOOGLE_TOKEN_REQUEST = """
            {
              "idToken": "google-id-token",
              "appId": "507f1f77bcf86cd799439011",
              "nonce": "optional-nonce"
            }
            """;

    public static final String USER_TOKEN_RESPONSE = """
            {
              "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
              "refreshToken": "eyJhbGciOiJSUzI1NiJ9...",
              "tokenType": "Bearer",
              "expiresIn": 900,
              "refreshTokenExpiresIn": 2592000
            }
            """;

    public static final String OWNER_TOKEN_REFRESH_RESPONSE = """
            {
              "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
              "refreshToken": "eyJhbGciOiJSUzI1NiJ9...",
              "tokenType": "Bearer",
              "expiresIn": 900,
              "refreshTokenExpiresIn": 2592000
            }
            """;

    public static final String USER_TOKEN_REFRESH_RESPONSE = """
            {
              "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
              "refreshToken": "eyJhbGciOiJSUzI1NiJ9...",
              "tokenType": "Bearer",
              "expiresIn": 900,
              "refreshTokenExpiresIn": 2592000
            }
            """;
}
