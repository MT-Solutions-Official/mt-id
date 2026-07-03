package com.mtsolutions.application.exception;

import com.mtsolutions.domain.constant.Errors;
import com.mtsolutions.domain.constant.Origin;

public class ApplicationOwnerNotFoundException extends MtIdException {

    private static final Errors ERROR_CODE = Errors.APPLICATION_OWNER_NOT_FOUND;
    private static final Integer STATUS_CODE = 404;
    private static final String DEFAULT_MESSAGE = Errors.APPLICATION_OWNER_NOT_FOUND.getDisplayName();

    public ApplicationOwnerNotFoundException() {
        super(DEFAULT_MESSAGE, STATUS_CODE, ERROR_CODE, Origin.MT_ID);
    }
}
