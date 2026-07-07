package com.mtsolutions.application.exception;

import com.mtsolutions.domain.constant.Errors;
import com.mtsolutions.domain.constant.Origin;

public class ViaCepInvalidCepException extends MtIdException {

    private static final Errors ERROR_CODE = Errors.VIACEP_INVALID_CEP;
    private static final Integer STATUS_CODE = 400;

    public ViaCepInvalidCepException(String externalResponse) {
        super(ERROR_CODE.getDisplayName(), STATUS_CODE, ERROR_CODE, Origin.VIACEP, externalResponse);
    }
}
