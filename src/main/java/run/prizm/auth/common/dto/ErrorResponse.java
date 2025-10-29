package run.prizm.auth.common.dto;

import run.prizm.auth.common.constant.ErrorCode;

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
