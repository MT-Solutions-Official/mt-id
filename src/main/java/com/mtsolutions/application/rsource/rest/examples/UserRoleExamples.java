package com.mtsolutions.application.rsource.rest.examples;

public class UserRoleExamples {

    public static final String CREATE_USER_ROLE = """
            {
              "roleName": "admin"
            }
            """;

    public static final String USER_ROLE_CREATED = """
            {
              "userRoleId": "64a7f8b9c0e2a1b3d4f5e6b1",
              "appId": "507f1f77bcf86cd799439011",
              "roleName": "admin"
            }
            """;

    public static final String USER_ROLE_UPDATED = """
            {
              "userRoleId": "64a7f8b9c0e2a1b3d4f5e6b1",
              "appId": "507f1f77bcf86cd799439011",
              "roleName": "manager"
            }
            """;

    public static final String UPDATE_USER_ROLE = """
            {
              "userRoleId": "64a7f8b9c0e2a1b3d4f5e6b1",
              "roleName": "manager"
            }
            """;

    public static final String USER_ROLE_LIST = """
            [
              {
                "userRoleId": "64a7f8b9c0e2a1b3d4f5e6b1",
                "appId": "507f1f77bcf86cd799439011",
                "roleName": "ADMIN"
              },
              {
                "userRoleId": "64a7f8b9c0e2a1b3d4f5e6b2",
                "appId": "507f1f77bcf86cd799439011",
                "roleName": "MANAGER"
              }
            ]
            """;
}
