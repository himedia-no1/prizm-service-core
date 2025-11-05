package run.prizm.core.auth.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import run.prizm.core.config.AuthConfigProperties;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JwtClaimsExtractor {

    private final AuthConfigProperties authConfigProperties;

    public Claims extractClaims(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(authConfigProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}