package run.prizm.core.admin.service.token;

import org.springframework.stereotype.Component;
import run.prizm.core.admin.entity.Admin;
import run.prizm.core.common.constant.JwtClaim;
import run.prizm.core.common.constant.UserType;
import run.prizm.core.common.dto.TokenClaims;

import java.util.HashMap;
import java.util.Map;

@Component
public class AdminTokenClaimsMapper {

    public TokenClaims toAccessClaims(Admin admin) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaim.LOGIN_ID.getClaimName(), admin.getLoginId());
        claims.put(JwtClaim.ROLE.getClaimName(), admin.getRole().getValue());
        return TokenClaims.of(admin.getId().toString(), UserType.ADMIN.getValue(), claims);
    }

    public TokenClaims toRefreshClaims(Admin admin) {
        return TokenClaims.of(admin.getId().toString(), UserType.ADMIN.getValue(), null);
    }
}