package com.mtsolutions.application.exception;

import com.mtsolutions.domain.constant.Errors;
import com.mtsolutions.domain.constant.Origin;

public class EmailAlreadyVerifiedException extends MtIdException {

    private static final Errors ERROR_CODE = Errors.EMAIL_ALREADY_VERIFIED;
    private static final Integer STATUS_CODE = 409;
    private static final String DEFAULT_MESSAGE = Errors.EMAIL_ALREADY_VERIFIED.getDisplayName();

    public EmailAlreadyVerifiedException() {
        super(DEFAULT_MESSAGE, STATUS_CODE, ERROR_CODE, Origin.MT_ID);
    }
}

