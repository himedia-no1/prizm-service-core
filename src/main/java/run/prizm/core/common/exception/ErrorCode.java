package run.prizm.core.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "Invalid input value"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "Internal server error"),
    
    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "Unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "A002", "Forbidden"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "Invalid token"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A004", "Token expired"),
    
    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "User not found"),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "U002", "User already exists"),
    USER_BANNED(HttpStatus.FORBIDDEN, "U003", "User is banned"),
    
    // Workspace
    WORKSPACE_NOT_FOUND(HttpStatus.NOT_FOUND, "W001", "Workspace not found"),
    WORKSPACE_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "W002", "Workspace user not found"),
    WORKSPACE_USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "W003", "Workspace user already exists"),
    INSUFFICIENT_PERMISSION(HttpStatus.FORBIDDEN, "W004", "Insufficient permission"),
    OWNER_CANNOT_LEAVE(HttpStatus.BAD_REQUEST, "W005", "Owner cannot leave workspace"),
    
    // Category
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CT001", "Category not found"),
    
    // Channel
    CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "CH001", "Channel not found"),
    CHANNEL_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CH002", "Channel access denied"),
    
    // Group
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "G001", "Group not found"),
    CANNOT_ASSIGN_GUEST_TO_GROUP(HttpStatus.BAD_REQUEST, "G002", "Cannot assign GUEST users to groups"),
    
    // Invite
    INVITE_NOT_FOUND(HttpStatus.NOT_FOUND, "I001", "Invite not found"),
    INVITE_EXPIRED(HttpStatus.BAD_REQUEST, "I002", "Invite expired"),
    INVITE_USAGE_LIMIT_REACHED(HttpStatus.BAD_REQUEST, "I003", "Invite usage limit reached"),
    INVITE_RESTRICTED(HttpStatus.FORBIDDEN, "I004", "Invite restricted to specific users"),
    
    // File
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "F001", "File not found"),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F002", "File upload failed");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
