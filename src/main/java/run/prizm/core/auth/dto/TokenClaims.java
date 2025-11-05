package run.prizm.core.auth.dto;

import java.util.Collections;
import java.util.Map;

public record TokenClaims(
    String subject,
    String type,
    Map<String, Object> additionalClaims
) {
    public TokenClaims {
        additionalClaims = additionalClaims != null
                ? Collections.unmodifiableMap(additionalClaims)
                : Collections.emptyMap();
    }

    public static TokenClaims of(String subject, String type, Map<String, Object> additionalClaims) {
        return new TokenClaims(subject, type, additionalClaims);
    }
}