package com.mtsolutions.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorInfo {
    private String errorCode;
    private String message;
    private Integer status;
    private String path;
    private String stackTrace;
    private LocalDateTime timestamp;
    private String origin;
    private String externalResponse;
}
