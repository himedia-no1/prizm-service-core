package run.prizm.core.auth.dto;

import run.prizm.core.auth.constant.ErrorCode;

import java.time.ZonedDateTime;

public record ErrorResponse(
    String code,
    String message,
    ZonedDateTime timestamp
) {
    public ErrorResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage(), ZonedDateTime.now());
    }

    public ErrorResponse(ErrorCode errorCode, String message) {
        this(errorCode.getCode(), message, ZonedDateTime.now());
    }
}