package com.mtsolutions.application.exception;

import com.mtsolutions.domain.constant.Errors;
import com.mtsolutions.domain.constant.Origin;

public class ZippopotamZipNotFoundException extends MtIdException {

    private static final Errors ERROR_CODE = Errors.ZIPPOPOTAM_ZIP_NOT_FOUND;
    private static final Integer STATUS_CODE = 404;

    public ZippopotamZipNotFoundException() {
        super(ERROR_CODE.getDisplayName(), STATUS_CODE, ERROR_CODE, Origin.ZIPPOPOTAM, null);
    }

    public ZippopotamZipNotFoundException(String externalResponse) {
        super(ERROR_CODE.getDisplayName(), STATUS_CODE, ERROR_CODE, Origin.ZIPPOPOTAM, externalResponse);
    }
}
