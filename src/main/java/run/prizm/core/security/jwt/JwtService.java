package run.prizm.core.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import run.prizm.core.properties.AuthProperties;
import run.prizm.core.user.entity.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtService {

    private static final String ROLE_CLAIM = "role";
    private static final String ID_CLAIM = "id";
    private static final String DEFAULT_ROLE = "USER";

    private final AuthProperties authProperties;

    public String generateAccessToken(User user) {
        return generateAccessToken(user.getId(), determineRole(user));
    }

    public String generateAccessToken(Long userId, String role) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiration = new Date(now + authProperties.getJwt()
                                                       .getAccessTokenExpiration());

        SecretKey key = getSigningKey();

        return Jwts.builder()
                   .claim(ROLE_CLAIM, role)
                   .claim(ID_CLAIM, userId)
                   .issuedAt(issuedAt)
                   .expiration(expiration)
                   .signWith(key)
                   .compact();
    }

    public String generateRefreshToken() {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiration = new Date(now + authProperties.getJwt()
                                                       .getRefreshTokenExpiration());

        SecretKey key = getSigningKey();

        return Jwts.builder()
                   .issuedAt(issuedAt)
                   .expiration(expiration)
                   .signWith(key)
                   .compact();
    }

    public long getAccessTokenExpirationInSeconds() {
        return authProperties.getJwt()
                             .getAccessTokenExpiration() / 1000;
    }

    public long getRefreshTokenExpirationInSeconds() {
        return authProperties.getJwt()
                             .getRefreshTokenExpiration() / 1000;
    }

    public boolean validateToken(String token) {
        try {
            SecretKey key = getSigningKey();
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getIdFromToken(String token) {
        Claims claims = extractClaims(token);
        if (claims == null) {
            return null;
        }
        Object idObj = claims.get(ID_CLAIM);
        if (idObj instanceof Number) {
            return ((Number) idObj).longValue();
        }
        return null;
    }

    public String getRoleFromToken(String token) {
        Claims claims = extractClaims(token);
        return claims != null ? claims.get(ROLE_CLAIM, String.class) : null;
    }

    public Claims extractClaims(String token) {
        try {
            SecretKey key = getSigningKey();
            return Jwts.parser()
                       .verifyWith(key)
                       .build()
                       .parseSignedClaims(token)
                       .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public boolean isExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration == null || expiration.before(new Date());
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(authProperties.getJwt()
                                                .getSecret()
                                                .getBytes(StandardCharsets.UTF_8));
    }

    private String determineRole(User user) {
        // USER 는 기본 권한이며, 향후 ADMIN 등 추가 시 이 로직을 확장한다.
        return DEFAULT_ROLE;
    }
}
