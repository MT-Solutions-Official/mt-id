package com.mtsolutions.domain.model;

import com.mtsolutions.domain.constant.OwnerRole;
import com.mtsolutions.domain.entity.ClientApplication;

public final class AppOwnerAccess {

    private AppOwnerAccess() {
    }

    public static AppOwnerMembership membership(ClientApplication application, String ownerId) {
        if (application == null || application.getOwners() == null || ownerId == null) {
            return null;
        }
        return application.getOwners().stream()
                .filter(item -> item != null && ownerId.equals(item.getOwnerId()))
                .findFirst()
                .orElse(null);
    }

    public static boolean isMember(ClientApplication application, String ownerId) {
        return membership(application, ownerId) != null;
    }

    public static boolean isWriter(ClientApplication application, String ownerId) {
        AppOwnerMembership membership = membership(application, ownerId);
        return membership != null && membership.getRole() == OwnerRole.OWNER_WRITER;
    }

    public static long writerCount(ClientApplication application) {
        if (application == null || application.getOwners() == null) {
            return 0;
        }
        return application.getOwners().stream()
                .filter(item -> item != null && item.getRole() == OwnerRole.OWNER_WRITER)
                .count();
    }
}
