package run.prizm.core.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import run.prizm.core.auth.entity.User;

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
                   .setSubject(user.getUuid().toString())
                   .claim("type", "user")
                   .claim("email", user.getGlobalEmail())
                   .claim("name", user.getGlobalName())
                   .claim("provider", user.getAuthProvider().name())
                   .setIssuedAt(issuedAt)
                   .setExpiration(expiration)
                   .signWith(key, SignatureAlgorithm.HS256)
                   .compact();
    }

    public String generateRefreshToken(User user) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiration = new Date(now + refreshTokenExpiration);

        SecretKey key = getSigningKey();

        return Jwts.builder()
                   .setSubject(user.getUuid().toString())
                   .claim("type", "user")
                   .setIssuedAt(issuedAt)
                   .setExpiration(expiration)
                   .signWith(key, SignatureAlgorithm.HS256)
                   .compact();
    }

    public long getAccessTokenExpirationInSeconds() {
        return accessTokenExpiration / 1000;
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

    public String getTypeFromToken(String token) {
        Claims claims = extractClaims(token);
        return claims != null ? claims.get("type", String.class) : null;
    }

    public String getSubjectFromToken(String token) {
        Claims claims = extractClaims(token);
        return claims != null ? claims.getSubject() : null;
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