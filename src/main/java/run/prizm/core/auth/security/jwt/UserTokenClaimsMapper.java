package run.prizm.core.auth.security.jwt;

import org.springframework.stereotype.Component;
import run.prizm.core.auth.constant.JwtClaim;
import run.prizm.core.auth.constant.UserType;
import run.prizm.core.auth.dto.TokenClaims;
import run.prizm.core.auth.domain.User;

import java.util.HashMap;
import java.util.Map;

@Component
public class UserTokenClaimsMapper {

    public TokenClaims toAccessClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaim.EMAIL.getClaimName(), user.getGlobalEmail());
        claims.put(JwtClaim.NAME.getClaimName(), user.getGlobalName());
        claims.put(JwtClaim.PROVIDER.getClaimName(), user.getAuthProvider().getValue());
        return TokenClaims.of(user.getUuid().toString(), UserType.USER.getValue(), claims);
    }

    public TokenClaims toRefreshClaims(User user) {
        return TokenClaims.of(user.getUuid().toString(), UserType.USER.getValue(), null);
    }
}