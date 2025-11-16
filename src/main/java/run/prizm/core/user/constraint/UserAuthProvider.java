package run.prizm.core.user.constraint;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserAuthProvider {
    GITHUB, GOOGLE
}