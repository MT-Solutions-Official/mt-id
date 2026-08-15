package com.mtsolutions.application.exception;

import com.mtsolutions.domain.constant.Errors;
import com.mtsolutions.domain.constant.Origin;

public class EmailNotVerifiedException extends MtIdException {

    private static final Errors ERROR_CODE = Errors.EMAIL_NOT_VERIFIED;
    private static final Integer STATUS_CODE = 403;

    public EmailNotVerifiedException() {
        super(ERROR_CODE.getDisplayName(), STATUS_CODE, ERROR_CODE, Origin.MT_ID);
    }
}
