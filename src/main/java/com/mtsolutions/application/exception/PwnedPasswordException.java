package com.mtsolutions.application.exception;

import com.mtsolutions.domain.constant.Errors;
import com.mtsolutions.domain.constant.Origin;

public class PwnedPasswordException extends MtIdException {

    private static final Errors ERROR_CODE = Errors.PASSWORD_COMPROMISED;
    private static final Integer STATUS_CODE = 400;

    public PwnedPasswordException() {
        super(ERROR_CODE.getDisplayName(), STATUS_CODE, ERROR_CODE, Origin.MT_ID);
    }
}
