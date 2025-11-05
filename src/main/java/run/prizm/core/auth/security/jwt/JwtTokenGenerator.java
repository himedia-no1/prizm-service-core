package run.prizm.core.auth.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import run.prizm.core.config.AuthConfigProperties;
import run.prizm.core.auth.dto.TokenClaims;
import run.prizm.core.auth.constant.JwtClaim;

import java.util.Date;
import javax.crypto.SecretKey;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JwtTokenGenerator {

    private final AuthConfigProperties authConfigProperties;

    public String generateAccessToken(TokenClaims tokenClaims) {
        return generateToken(tokenClaims, authConfigProperties.getJwt().getAccessTokenExpiration(), true);
    }

    public String generateRefreshToken(TokenClaims tokenClaims) {
        return generateToken(tokenClaims, authConfigProperties.getJwt().getRefreshTokenExpiration(), false);
    }

    public long getAccessTokenExpirationInSeconds() {
        return authConfigProperties.getJwt().getAccessTokenExpiration() / 1000;
    }

    private String generateToken(TokenClaims tokenClaims, long expirationInMillis, boolean includeClaims) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiration = new Date(now + expirationInMillis);

        SecretKey key = Keys.hmacShaKeyFor(authConfigProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));

        var builder = Jwts.builder()
                          .setSubject(tokenClaims.subject())
                          .claim(JwtClaim.TYPE.getClaimName(), tokenClaims.type())
                          .setIssuedAt(issuedAt)
                          .setExpiration(expiration);

        if (includeClaims) {
            builder.addClaims(tokenClaims.additionalClaims());
        }

        return builder.signWith(key, SignatureAlgorithm.HS256)
                      .compact();
    }
}