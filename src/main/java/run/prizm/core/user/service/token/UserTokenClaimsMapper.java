package run.prizm.core.user.service.token;

import org.springframework.stereotype.Component;
import run.prizm.core.common.constant.JwtClaim;
import run.prizm.core.common.constant.UserType;
import run.prizm.core.common.dto.TokenClaims;
import run.prizm.core.user.entity.User;

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