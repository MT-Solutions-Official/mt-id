package com.mtsolutions.application.exception;

import com.mtsolutions.domain.constant.Errors;
import com.mtsolutions.domain.constant.Origin;

public class KodePosApiException extends MtIdException {

    private static final Errors ERROR_CODE = Errors.KODEPOS_API_UNAVAILABLE;
    private static final Integer STATUS_CODE = 503;

    public KodePosApiException(String externalResponse) {
        super(ERROR_CODE.getDisplayName(), STATUS_CODE, ERROR_CODE, Origin.KODEPOS, externalResponse);
    }
}
