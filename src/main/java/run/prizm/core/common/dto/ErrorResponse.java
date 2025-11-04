package run.prizm.core.common.dto;

import run.prizm.core.common.constant.ErrorCode;

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