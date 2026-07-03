package com.mtsolutions.application.exception;

import com.mtsolutions.domain.constant.Errors;
import com.mtsolutions.domain.constant.Origin;

public class UserRoleAlreadyExistsException extends MtIdException {

    private static final Errors ERROR_CODE = Errors.USER_ROLE_ALREADY_EXISTS;
    private static final Integer STATUS_CODE = 409;

    public UserRoleAlreadyExistsException(String roleName) {
        super("User role already exists: " + roleName, STATUS_CODE, ERROR_CODE, Origin.MT_ID);
    }
}
