package com.mtsolutions.application.exception;

import com.mtsolutions.domain.constant.Errors;
import com.mtsolutions.domain.constant.Origin;

public class UserRoleNotFoundException extends MtIdException {

    private static final Errors ERROR_CODE = Errors.USER_ROLE_NOT_FOUND;
    private static final Integer STATUS_CODE = 404;
    private static final String DEFAULT_MESSAGE = Errors.USER_ROLE_NOT_FOUND.getDisplayName();

    public UserRoleNotFoundException() {
        super(DEFAULT_MESSAGE, STATUS_CODE, ERROR_CODE, Origin.MT_ID);
    }

    public UserRoleNotFoundException(String roleName) {
        super("User role not found: " + roleName, STATUS_CODE, ERROR_CODE, Origin.MT_ID);
    }
}
