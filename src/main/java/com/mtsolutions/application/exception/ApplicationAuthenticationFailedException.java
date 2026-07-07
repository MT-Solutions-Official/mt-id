package com.mtsolutions.application.exception;

import com.mtsolutions.domain.constant.Errors;
import com.mtsolutions.domain.constant.Origin;

public class ApplicationAuthenticationFailedException extends MtIdException {

    private static final Errors ERROR_CODE = Errors.APPLICATION_AUTHENTICATION_FAILED;
    private static final Integer STATUS_CODE = 401;
    private static final String DEFAULT_MESSAGE = Errors.APPLICATION_AUTHENTICATION_FAILED.getDisplayName();

    public ApplicationAuthenticationFailedException() {
        super(DEFAULT_MESSAGE, STATUS_CODE, ERROR_CODE, Origin.MT_ID);
    }
}
