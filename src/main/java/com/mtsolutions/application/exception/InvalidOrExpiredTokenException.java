package com.mtsolutions.application.exception;

import com.mtsolutions.domain.constant.Errors;
import com.mtsolutions.domain.constant.Origin;

public class InvalidOrExpiredTokenException extends MtIdException {

    private static final Errors ERROR_CODE = Errors.INVALID_OR_EXPIRED_TOKEN;
    private static final Integer STATUS_CODE = 400;
    private static final String DEFAULT_MESSAGE = Errors.INVALID_OR_EXPIRED_TOKEN.getDisplayName();

    public InvalidOrExpiredTokenException() {
        super(DEFAULT_MESSAGE, STATUS_CODE, ERROR_CODE, Origin.MT_ID);
    }
}

