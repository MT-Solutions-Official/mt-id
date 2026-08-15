package com.mtsolutions.application.exception;

import com.mtsolutions.domain.constant.Errors;
import com.mtsolutions.domain.constant.Origin;

public class AccountDisabledException extends MtIdException {

    private static final Errors ERROR_CODE = Errors.ACCOUNT_DISABLED;
    private static final Integer STATUS_CODE = 403;

    public AccountDisabledException() {
        super(ERROR_CODE.getDisplayName(), STATUS_CODE, ERROR_CODE, Origin.MT_ID);
    }
}
