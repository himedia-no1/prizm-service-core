package run.prizm.core.security.cookie;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import run.prizm.core.properties.AuthProperties;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class CookieService {

    private final AuthProperties authProperties;

    public void setAccessToken(HttpServletResponse response, String token) {
        String cookieHeader = buildSetCookieHeader("access_token", token, getAccessTokenMaxAge(), authProperties.getCookie()
                                                                                                                .getPath());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieHeader);
    }

    public void setRefreshToken(HttpServletResponse response, String token) {
        String cookieHeader = buildSetCookieHeader("refresh_token", token, getRefreshTokenMaxAge(), authProperties.getCookie()
                                                                                                                  .getPath());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieHeader);
    }

    public void deleteAccessToken(HttpServletResponse response) {
        String cookieHeader = buildDeleteCookieHeader("access_token", authProperties.getCookie()
                                                                                    .getPath());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieHeader);
    }

    public void deleteRefreshToken(HttpServletResponse response) {
        String cookieHeader = buildDeleteCookieHeader("refresh_token", authProperties.getCookie()
                                                                                     .getPath());
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
        return (int) (authProperties.getJwt()
                                    .getAccessTokenExpiration() / 1000);
    }

    private int getRefreshTokenMaxAge() {
        return (int) (authProperties.getJwt()
                                    .getRefreshTokenExpiration() / 1000);
    }

    private void appendCookieAttributes(StringBuilder header) {
        if (authProperties.getCookie()
                          .getHttpOnly()) {
            header.append("; HttpOnly");
        }
        if (authProperties.getCookie()
                          .getSecure()) {
            header.append("; Secure");
        }
        if (isNotEmpty(authProperties.getCookie()
                                     .getSameSite())) {
            header.append("; SameSite=")
                  .append(authProperties.getCookie()
                                        .getSameSite());
        }
        if (isNotEmpty(authProperties.getCookie()
                                     .getDomain())) {
            header.append("; Domain=")
                  .append(authProperties.getCookie()
                                        .getDomain());
        }
    }

    private boolean isNotEmpty(String value) {
        return value != null && !value.isEmpty();
    }
}