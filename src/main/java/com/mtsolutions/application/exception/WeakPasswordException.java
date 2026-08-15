package com.mtsolutions.application.exception;

import com.mtsolutions.domain.constant.Errors;
import com.mtsolutions.domain.constant.Origin;

public class WeakPasswordException extends MtIdException {

    private static final Errors ERROR_CODE = Errors.WEAK_PASSWORD;
    private static final Integer STATUS_CODE = 400;

    public WeakPasswordException() {
        super(ERROR_CODE.getDisplayName(), STATUS_CODE, ERROR_CODE, Origin.MT_ID);
    }
}
