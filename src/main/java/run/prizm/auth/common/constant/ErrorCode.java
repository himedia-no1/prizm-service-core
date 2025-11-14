package run.prizm.auth.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Server errors
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER001", "Internal server error"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON001", "Bad request"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON002", "Not found"),

    // Token errors
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN001", "Invalid token"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN002", "Expired token"),
    MALFORMED_TOKEN(HttpStatus.BAD_REQUEST, "TOKEN003", "Malformed token"),

    // Authentication errors
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH001", "Unauthorized"),
    MISSING_USER_INFO(HttpStatus.UNAUTHORIZED, "AUTH002", "Missing user information"),
    MISSING_ADMIN_INFO(HttpStatus.UNAUTHORIZED, "AUTH003", "Missing admin information"),

    // User errors
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER001", "User not found"),
    INVALID_USER_UUID(HttpStatus.BAD_REQUEST, "USER002", "Invalid user UUID"),

    // Admin errors
    ADMIN_NOT_FOUND(HttpStatus.NOT_FOUND, "ADMIN001", "Admin not found"),
    INVALID_ADMIN_ID(HttpStatus.BAD_REQUEST, "ADMIN002", "Invalid admin ID"),

    // OAuth2 errors
    OAUTH2_PROVIDER_NOT_FOUND(HttpStatus.NOT_FOUND, "OAUTH001", "OAuth2 provider not found"),
    UNSUPPORTED_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST, "OAUTH002", "Unsupported OAuth2 provider"),

    // File errors
    FILE_EMPTY(HttpStatus.BAD_REQUEST, "FILE001", "File is empty"),
    FILE_INVALID_NAME(HttpStatus.BAD_REQUEST, "FILE002", "File name is invalid"),
    FILE_UNSUPPORTED_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "FILE003", "Unsupported file type"),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE004", "File upload failed");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
