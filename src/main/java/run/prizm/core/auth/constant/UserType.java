package run.prizm.core.auth.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserType {
    USER("user"),
    ADMIN("admin");

    private final String value;
}