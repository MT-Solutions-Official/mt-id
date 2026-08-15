package com.mtsolutions.application.exception;

import com.mtsolutions.domain.constant.Errors;
import com.mtsolutions.domain.constant.Origin;

public class EmailAlreadyExistsException extends MtIdException {

    private static final Errors ERROR_CODE = Errors.EMAIL_ALREADY_EXISTS;
    private static final Integer STATUS_CODE = 409;

    public EmailAlreadyExistsException() {
        super(ERROR_CODE.getDisplayName(), STATUS_CODE, ERROR_CODE, Origin.MT_ID);
    }
}
