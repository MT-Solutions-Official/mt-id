package com.mtsolutions.application.exception;

import com.mtsolutions.domain.constant.Errors;
import com.mtsolutions.domain.constant.Origin;

public class TooManyRequestsException extends MtIdException {

    private static final Errors ERROR_CODE = Errors.RATE_LIMIT_EXCEEDED;
    private static final Integer STATUS_CODE = 429;
    private static final String DEFAULT_MESSAGE = Errors.RATE_LIMIT_EXCEEDED.getDisplayName();

    public TooManyRequestsException() {
        super(DEFAULT_MESSAGE, STATUS_CODE, ERROR_CODE, Origin.MT_ID);
    }
}

