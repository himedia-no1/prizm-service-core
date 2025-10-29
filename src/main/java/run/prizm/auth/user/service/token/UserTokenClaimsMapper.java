package run.prizm.auth.user.service.token;

import org.springframework.stereotype.Component;
import run.prizm.auth.common.constant.JwtClaim;
import run.prizm.auth.common.constant.UserType;
import run.prizm.auth.common.dto.TokenClaims;
import run.prizm.auth.user.entity.User;

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