package run.prizm.core.auth.security.jwt;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import run.prizm.core.auth.constant.JwtClaim;
import run.prizm.core.auth.dto.TokenClaims;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtTokenGenerator tokenGenerator;
    private final JwtTokenValidator tokenValidator;
    private final JwtClaimsExtractor claimsExtractor;

    public String generateAccessToken(TokenClaims tokenClaims) {
        return tokenGenerator.generateAccessToken(tokenClaims);
    }

    public String generateRefreshToken(TokenClaims tokenClaims) {
        return tokenGenerator.generateRefreshToken(tokenClaims);
    }

    public boolean validateToken(String token) {
        return tokenValidator.validateToken(token);
    }

    public String getTypeFromToken(String token) {
        Claims claims = claimsExtractor.extractClaims(token);
        return claims != null ? claims.get(JwtClaim.TYPE.getClaimName(), String.class) : null;
    }

    public String getSubjectFromToken(String token) {
        Claims claims = claimsExtractor.extractClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    public long getAccessTokenExpirationInSeconds() {
        return tokenGenerator.getAccessTokenExpirationInSeconds();
    }
}