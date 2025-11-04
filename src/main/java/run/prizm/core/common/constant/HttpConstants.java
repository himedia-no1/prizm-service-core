package run.prizm.core.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HttpConstants {
    // Headers
    HEADER_USER_TYPE("X-User-Type"),
    HEADER_USER_UUID("X-User-UUID"),
    HEADER_ADMIN_ID("X-Admin-ID"),
    
    // Cookies
    COOKIE_REFRESH_TOKEN("refresh_token"),
    
    // Paths
    PATH_ROOT("/");

    private final String value;
}