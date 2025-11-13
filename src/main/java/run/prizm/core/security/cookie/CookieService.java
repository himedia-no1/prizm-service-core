package run.prizm.core.security.cookie;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    @Value("${prizm.auth.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${prizm.auth.cookie.http-only}")
    private boolean httpOnly;

    @Value("${prizm.auth.cookie.secure}")
    private boolean secure;

    @Value("${prizm.auth.cookie.same-site}")
    private String sameSite;

    @Value("${prizm.auth.cookie.domain}")
    private String domain;

    public void setRefreshToken(HttpServletResponse response, String token) {
        String cookieHeader = buildSetCookieHeader("refresh_token", token, getRefreshTokenMaxAge(), "/");
        response.addHeader(HttpHeaders.SET_COOKIE, cookieHeader);
    }

    public void deleteRefreshToken(HttpServletResponse response) {
        String cookieHeader = buildDeleteCookieHeader("refresh_token", "/");
        response.addHeader(HttpHeaders.SET_COOKIE, cookieHeader);
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
            header.append("; SameSite=").append(sameSite);
        }
        if (isNotEmpty(domain)) {
            header.append("; Domain=").append(domain);
        }
    }

    private boolean isNotEmpty(String value) {
        return value != null && !value.isEmpty();
    }
}
