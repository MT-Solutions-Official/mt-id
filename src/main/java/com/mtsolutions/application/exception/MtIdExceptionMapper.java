package com.mtsolutions.application.exception;

import com.mtsolutions.domain.model.ErrorInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class MtIdExceptionMapper implements ExceptionMapper<MtIdException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(MtIdException exception) {

        ErrorInfo errorInfo = ErrorInfo.builder()
                .errorCode(exception.getErrorCode().name())
                .message(exception.getMessage())
                .status(exception.getHttpStatus())
                .path(uriInfo.getPath())
                .timestamp(exception.getTimestamp())
                .origin(exception.getOrigin() != null ? exception.getOrigin().getDisplayName() : null)
                .externalResponse(exception.getExternalResponse())
                .build();

        return Response.status(exception.getHttpStatus()).entity(errorInfo).build();
    }
}
