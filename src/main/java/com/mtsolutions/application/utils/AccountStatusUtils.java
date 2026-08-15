package com.mtsolutions.application.utils;

import com.mtsolutions.domain.entity.Owner;
import com.mtsolutions.domain.entity.User;

import java.time.LocalDateTime;

public final class AccountStatusUtils {

    private AccountStatusUtils() {}

    public static boolean isDisabled(User user) {
        return user == null || isDisabled(user.getActive(), user.getDisabledAt());
    }

    public static boolean isDisabled(Owner owner) {
        return owner == null || isDisabled(owner.getActive(), owner.getDisabledAt());
    }

    public static boolean isDisabled(Boolean active, LocalDateTime disabledAt) {
        return disabledAt != null || Boolean.FALSE.equals(active);
    }
}
