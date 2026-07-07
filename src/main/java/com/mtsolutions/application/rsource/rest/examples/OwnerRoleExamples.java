package com.mtsolutions.application.rsource.rest.examples;

public class OwnerRoleExamples {

    public static final String CREATE_OWNER_ROLE = """
            {
              "roleName": "admin"
            }
            """;

    public static final String OWNER_ROLE_CREATED = """
            {
              "ownerRoleId": "64a7f8b9c0e2a1b3d4f5e6c1",
              "roleName": "ADMIN"
            }
            """;

    public static final String OWNER_ROLE_UPDATED = """
            {
              "ownerRoleId": "64a7f8b9c0e2a1b3d4f5e6c1",
              "roleName": "MANAGER"
            }
            """;

    public static final String UPDATE_OWNER_ROLE = """
            {
              "ownerRoleId": "64a7f8b9c0e2a1b3d4f5e6c1",
              "roleName": "manager"
            }
            """;

    public static final String OWNER_ROLE_LIST = """
            [
              {
                "ownerRoleId": "64a7f8b9c0e2a1b3d4f5e6c1",
                "roleName": "ADMIN"
              },
              {
                "ownerRoleId": "64a7f8b9c0e2a1b3d4f5e6c2",
                "roleName": "MANAGER"
              }
            ]
            """;
}
