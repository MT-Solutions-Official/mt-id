package com.mtsolutions.application.exception;

import com.mtsolutions.domain.constant.Errors;
import com.mtsolutions.domain.constant.Origin;

public class UserRoleNotFoundException extends MtIdException {

    private static final Errors ERROR_CODE = Errors.USER_ROLE_NOT_FOUND;
    private static final Integer STATUS_CODE = 404;

    public UserRoleNotFoundException(String roleName) {
        super("User role not found: " + roleName, STATUS_CODE, ERROR_CODE, Origin.MT_ID);
    }
}
