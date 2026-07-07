package com.mtsolutions.application.exception;

import com.mtsolutions.domain.constant.Errors;
import com.mtsolutions.domain.constant.Origin;

public class ViaCepCepNotFoundException extends MtIdException {

    private static final Errors ERROR_CODE = Errors.VIACEP_NOT_FOUND;
    private static final Integer STATUS_CODE = 422;

    public ViaCepCepNotFoundException() {
        super(ERROR_CODE.getDisplayName(), STATUS_CODE, ERROR_CODE, Origin.VIACEP);
    }
}
