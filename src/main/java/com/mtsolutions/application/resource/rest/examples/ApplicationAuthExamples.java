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
              "tokenType": "Bearer",
              "expiresIn": 86400
            }
            """;

    public static final String APP_TOKEN_RESPONSE = """
            {
              "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
              "tokenType": "Bearer",
              "expiresIn": 3600
            }
            """;
}
