package run.prizm.core.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "Invalid input value"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "Internal server error"),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "Unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "A002", "Forbidden"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "Invalid token"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A004", "Token expired"),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "User not found"),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "U002", "User already exists"),
    USER_BANNED(HttpStatus.FORBIDDEN, "U003", "User is banned"),

    WORKSPACE_NOT_FOUND(HttpStatus.NOT_FOUND, "W001", "Workspace not found"),
    WORKSPACE_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "W002", "Workspace user not found"),
    WORKSPACE_USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "W003", "Workspace user already exists"),
    INSUFFICIENT_PERMISSION(HttpStatus.FORBIDDEN, "W004", "Insufficient permission"),
    OWNER_CANNOT_LEAVE(HttpStatus.BAD_REQUEST, "W005", "Owner cannot leave workspace"),

    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CT001", "Category not found"),

    CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "CH001", "Channel not found"),
    CHANNEL_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CH002", "Channel access denied"),

    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "G001", "Group not found"),
    CANNOT_ASSIGN_GUEST_TO_GROUP(HttpStatus.BAD_REQUEST, "G002", "Cannot assign GUEST users to groups"),

    INVITE_NOT_FOUND(HttpStatus.NOT_FOUND, "I001", "Invite not found"),
    INVITE_EXPIRED(HttpStatus.BAD_REQUEST, "I002", "Invite expired"),
    INVITE_USAGE_LIMIT_REACHED(HttpStatus.BAD_REQUEST, "I003", "Invite usage limit reached"),
    INVITE_RESTRICTED(HttpStatus.FORBIDDEN, "I004", "Invite restricted to specific users"),

    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "F001", "File not found"),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F002", "File upload failed"),
    FILE_DOWNLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F003", "File download failed"),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F004", "File delete failed"),

    INVALID_LANGUAGE_CODE(HttpStatus.BAD_REQUEST, "T001", "Invalid language code"),
    TRANSLATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "T002", "Translation failed"),

    CACHE_OPERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "R001", "Cache operation failed"),

    INVALID_POSITION(HttpStatus.BAD_REQUEST, "P001", "Invalid position"),

    OWNER_DELEGATION_FORBIDDEN(HttpStatus.FORBIDDEN, "W006", "Only OWNER can delegate OWNER role"),
    CHANNEL_NOT_IN_WORKSPACE(HttpStatus.BAD_REQUEST, "W007", "Channel not in workspace"),
    USER_BANNED_FROM_WORKSPACE(HttpStatus.FORBIDDEN, "W008", "User is banned from this workspace"),
    USER_ALREADY_IN_WORKSPACE(HttpStatus.CONFLICT, "W009", "User already joined workspace"),
    INVALID_INVITE_PERMISSION(HttpStatus.FORBIDDEN, "W010", "User not allowed to create invite"),
    WORKSPACE_DELETED(HttpStatus.NOT_FOUND, "W011", "Workspace is deleted"),

    USER_DELETED(HttpStatus.FORBIDDEN, "U004", "User is deleted"),

    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "A005", "Refresh token not found"),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A006", "Invalid or expired refresh token"),
    REFRESH_TOKEN_NOT_IN_STORAGE(HttpStatus.UNAUTHORIZED, "A007", "Refresh token not found in storage"),
    INVALID_AUTHENTICATION(HttpStatus.UNAUTHORIZED, "A008", "Invalid authentication"),

    GUEST_INVITE_REQUIRES_ALLOWED_USERS(HttpStatus.BAD_REQUEST, "I005", "Guest invite requires allowed user IDs"),
    GUEST_INVITE_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "I006", "Only OWNER, MANAGER, or MEMBER with MANAGE permission can create guest invite"),
    MEMBER_REQUIRES_MANAGE_PERMISSION(HttpStatus.FORBIDDEN, "I007", "MEMBER requires MANAGE permission on this channel to create guest invite"),
    INVITE_NOT_FOR_WORKSPACE(HttpStatus.BAD_REQUEST, "I008", "Invite not for this workspace"),
    USER_NOT_ALLOWED_FOR_INVITE(HttpStatus.FORBIDDEN, "I009", "User not allowed to use this invite"),
    ALLOWED_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "I010", "Allowed user not found");

    private final HttpStatus status;
    private final String code;
    private final String message;
}