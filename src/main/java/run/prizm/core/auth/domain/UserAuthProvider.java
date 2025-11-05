package run.prizm.core.auth.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserAuthProvider {
    GITHUB("GITHUB"),
    GITLAB("GITLAB"),
    GOOGLE("GOOGLE"),
    MICROSOFT("MICROSOFT");

    private final String value;
}