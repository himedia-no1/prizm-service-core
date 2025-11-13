package run.prizm.core.auth.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserAuthProvider {
    GITHUB, GOOGLE
}