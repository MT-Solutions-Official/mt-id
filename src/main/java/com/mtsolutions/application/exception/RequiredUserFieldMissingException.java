package com.mtsolutions.application.exception;

import com.mtsolutions.domain.constant.Errors;
import com.mtsolutions.domain.constant.Origin;
import com.mtsolutions.domain.constant.UserRequiredField;

public class RequiredUserFieldMissingException extends MtIdException {

    private static final Errors ERROR_CODE = Errors.REQUIRED_USER_FIELD_MISSING;
    private static final Integer STATUS_CODE = 400;

    public RequiredUserFieldMissingException(UserRequiredField field) {
        super("Required user field is missing: " + field.name(), STATUS_CODE, ERROR_CODE, Origin.MT_ID);
    }
}
