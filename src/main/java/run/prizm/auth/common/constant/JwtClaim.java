package run.prizm.auth.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JwtClaim {
    TYPE("type"),
    EMAIL("email"),
    NAME("name"),
    PROVIDER("provider"),
    LOGIN_ID("loginId"),
    ROLE("role");

    private final String claimName;
}
