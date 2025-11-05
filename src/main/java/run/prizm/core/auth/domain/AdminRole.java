package run.prizm.core.auth.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdminRole {
    ADMIN("ADMIN"),
    SUPER_ADMIN("SUPER_ADMIN");

    private final String value;
}