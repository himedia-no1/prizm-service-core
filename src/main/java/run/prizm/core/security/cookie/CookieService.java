package run.prizm.core.security.cookie;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class CookieService {

    @Value("${prizm.auth.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${prizm.auth.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${prizm.auth.cookie.http-only}")
    private boolean httpOnly;

    @Value("${prizm.auth.cookie.secure}")
    private boolean secure;

    @Value("${prizm.auth.cookie.same-site}")
    private String sameSite;

    @Value("${prizm.auth.cookie.domain:}")
    private String domain;

    @Value("${prizm.auth.cookie.path:/}")
    private String path;

    public void setAccessToken(HttpServletResponse response, String token) {
        String cookieHeader = buildSetCookieHeader("access_token", token, getAccessTokenMaxAge(), path);
        response.addHeader(HttpHeaders.SET_COOKIE, cookieHeader);
    }

    public void setRefreshToken(HttpServletResponse response, String token) {
        String cookieHeader = buildSetCookieHeader("refresh_token", token, getRefreshTokenMaxAge(), path);
        response.addHeader(HttpHeaders.SET_COOKIE, cookieHeader);
    }

    public void deleteAccessToken(HttpServletResponse response) {
        String cookieHeader = buildDeleteCookieHeader("access_token", path);
        response.addHeader(HttpHeaders.SET_COOKIE, cookieHeader);
    }

    public void deleteRefreshToken(HttpServletResponse response) {
        String cookieHeader = buildDeleteCookieHeader("refresh_token", path);
        response.addHeader(HttpHeaders.SET_COOKIE, cookieHeader);
    }

    public String extractAccessTokenFromCookies(HttpServletRequest request) {
        return extractCookieValue(request, "access_token");
    }

    public String extractRefreshTokenFromCookies(HttpServletRequest request) {
        return extractCookieValue(request, "refresh_token");
    }

    private String extractCookieValue(HttpServletRequest request, String cookieName) {
        var cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .findFirst()
                .map(jakarta.servlet.http.Cookie::getValue)
                .orElse(null);
    }

    private String buildSetCookieHeader(String name, String value, int maxAgeSeconds, String path) {
        StringBuilder header = new StringBuilder();
        header.append(name)
              .append("=")
              .append(value)
              .append("; Path=")
              .append(path)
              .append("; Max-Age=")
              .append(maxAgeSeconds);

        appendCookieAttributes(header);

        return header.toString();
    }

    private String buildDeleteCookieHeader(String name, String path) {
        return buildSetCookieHeader(name, "", 0, path);
    }

    private int getAccessTokenMaxAge() {
        return (int) (accessTokenExpiration / 1000);
    }

    private int getRefreshTokenMaxAge() {
        return (int) (refreshTokenExpiration / 1000);
    }

    private void appendCookieAttributes(StringBuilder header) {
        if (httpOnly) {
            header.append("; HttpOnly");
        }
        if (secure) {
            header.append("; Secure");
        }
        if (isNotEmpty(sameSite)) {
            header.append("; SameSite=")
                  .append(sameSite);
        }
        if (isNotEmpty(domain)) {
            header.append("; Domain=")
                  .append(domain);
        }
    }

    private boolean isNotEmpty(String value) {
        return value != null && !value.isEmpty();
    }
}