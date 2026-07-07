package com.mtsolutions.application.exception;

import com.mtsolutions.domain.constant.Errors;
import com.mtsolutions.domain.constant.Origin;

public class OwnerRoleNotFoundException extends MtIdException {

    private static final Errors ERROR_CODE = Errors.OWNER_ROLE_NOT_FOUND;
    private static final Integer STATUS_CODE = 404;
    private static final String DEFAULT_MESSAGE = Errors.OWNER_ROLE_NOT_FOUND.getDisplayName();

    public OwnerRoleNotFoundException() {
        super(DEFAULT_MESSAGE, STATUS_CODE, ERROR_CODE, Origin.MT_ID);
    }

    public OwnerRoleNotFoundException(String roleName) {
        super("Owner role not found: " + roleName, STATUS_CODE, ERROR_CODE, Origin.MT_ID);
    }
}
