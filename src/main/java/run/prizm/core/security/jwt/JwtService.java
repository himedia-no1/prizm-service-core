package run.prizm.core.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import run.prizm.core.user.entity.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtService {

    @Value("${prizm.auth.jwt.secret}")
    private String jwtSecret;

    @Value("${prizm.auth.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${prizm.auth.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public String generateAccessToken(User user) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiration = new Date(now + accessTokenExpiration);

        SecretKey key = getSigningKey();

        return Jwts.builder()
                   .claim("role", "USER")
                   .claim("id", user.getId())
                   .issuedAt(issuedAt)
                   .expiration(expiration)
                   .signWith(key)
                   .compact();
    }

    public String generateRefreshToken() {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiration = new Date(now + refreshTokenExpiration);

        SecretKey key = getSigningKey();

        return Jwts.builder()
                   .issuedAt(issuedAt)
                   .expiration(expiration)
                   .signWith(key)
                   .compact();
    }

    public long getAccessTokenExpirationInSeconds() {
        return accessTokenExpiration / 1000;
    }

    public long getRefreshTokenExpirationInSeconds() {
        return refreshTokenExpiration / 1000;
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
        Object idObj = claims.get("id");
        if (idObj instanceof Number) {
            return ((Number) idObj).longValue();
        }
        return null;
    }

    public String getRoleFromToken(String token) {
        Claims claims = extractClaims(token);
        return claims != null ? claims.get("role", String.class) : null;
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

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}