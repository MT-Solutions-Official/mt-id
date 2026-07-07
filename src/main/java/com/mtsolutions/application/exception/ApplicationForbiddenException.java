package com.mtsolutions.application.exception;

import com.mtsolutions.domain.constant.Errors;
import com.mtsolutions.domain.constant.Origin;

public class ApplicationForbiddenException extends MtIdException {

    private static final Errors ERROR_CODE = Errors.APPLICATION_FORBIDDEN;
    private static final Integer STATUS_CODE = 403;
    private static final String DEFAULT_MESSAGE = Errors.APPLICATION_FORBIDDEN.getDisplayName();

    public ApplicationForbiddenException() {
        super(DEFAULT_MESSAGE, STATUS_CODE, ERROR_CODE, Origin.MT_ID);
    }
}
