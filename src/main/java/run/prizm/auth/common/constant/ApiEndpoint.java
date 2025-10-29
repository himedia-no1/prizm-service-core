package run.prizm.auth.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApiEndpoint {

    USER_OAUTH2_LOGIN_PAGE("/auth/user/oauth2"),
    USER_OAUTH2_AUTHORIZATION_BASE("/auth/user/oauth2"),
    USER_OAUTH2_REDIRECT_PATTERN("/auth/user/oauth2/*/callback"),

    FRONTEND_LOGIN_SUCCESS("/"),
    FRONTEND_LOGIN_FAILURE("/");

    private final String path;
}