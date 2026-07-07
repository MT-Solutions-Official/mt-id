package com.mtsolutions.application.exception;

import com.mtsolutions.domain.constant.Errors;
import com.mtsolutions.domain.constant.Origin;

public class KodePosZipNotFoundException extends MtIdException {

    private static final Errors ERROR_CODE = Errors.KODEPOS_ZIP_NOT_FOUND;
    private static final Integer STATUS_CODE = 404;

    public KodePosZipNotFoundException() {
        super(ERROR_CODE.getDisplayName(), STATUS_CODE, ERROR_CODE, Origin.KODEPOS, null);
    }

    public KodePosZipNotFoundException(String externalResponse) {
        super(ERROR_CODE.getDisplayName(), STATUS_CODE, ERROR_CODE, Origin.KODEPOS, externalResponse);
    }
}
