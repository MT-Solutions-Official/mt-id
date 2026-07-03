package com.mtsolutions.application.exception;

import com.mtsolutions.domain.constant.Errors;
import com.mtsolutions.domain.constant.Origin;
import lombok.Getter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

@Getter
public class MtIdException extends RuntimeException {

    private final Integer httpStatus;
    private final Errors errorCode;
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final Origin origin;
    private final String externalResponse;

    protected MtIdException(String message, Integer httpStatus, Errors errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.origin = null;
        this.externalResponse = null;
    }

    protected MtIdException(String message, Integer httpStatus, Errors errorCode, Origin origin) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.origin = origin;
        this.externalResponse = null;
    }

    protected MtIdException(String message, Integer httpStatus, Errors errorCode,
                            Origin origin, String externalResponse) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.origin = origin;
        this.externalResponse = externalResponse;
    }

    public String getStackTraceAsString() {
        StringWriter sw = new StringWriter();
        this.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
