package run.prizm.core.auth.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import run.prizm.core.config.AuthConfigProperties;

@Component
@RequiredArgsConstructor
public class CookieUtil {

    private final AuthConfigProperties authConfigProperties;

    public String buildSetCookieHeader(String name, String value, int maxAgeSeconds, String path) {
        AuthConfigProperties.Cookie cookieProps = authConfigProperties.getCookie();

        StringBuilder header = new StringBuilder();
        header.append(name)
              .append("=")
              .append(value)
              .append("; Path=")
              .append(path)
              .append("; Max-Age=")
              .append(maxAgeSeconds);

        appendCookieAttributes(header, cookieProps);

        return header.toString();
    }

    public String buildDeleteCookieHeader(String name, String path) {
        return buildSetCookieHeader(name, "", 0, path);
    }

    public int getRefreshTokenMaxAge() {
        return (int) (authConfigProperties.getJwt().getRefreshTokenExpiration() / 1000);
    }

    private void appendCookieAttributes(StringBuilder header, AuthConfigProperties.Cookie cookieProps) {
        if (cookieProps.isHttpOnly()) {
            header.append("; HttpOnly");
        }
        if (cookieProps.isSecure()) {
            header.append("; Secure");
        }
        if (isNotEmpty(cookieProps.getSameSite())) {
            header.append("; SameSite=").append(cookieProps.getSameSite());
        }
        if (isNotEmpty(cookieProps.getDomain())) {
            header.append("; Domain=").append(cookieProps.getDomain());
        }
    }

    private boolean isNotEmpty(String value) {
        return value != null && !value.isEmpty();
    }
}