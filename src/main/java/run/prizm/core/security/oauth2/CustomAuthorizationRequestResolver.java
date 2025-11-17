package run.prizm.core.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultResolver;

    @Value("${prizm.frontend.url}")
    private String frontendUrl;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/api/auth/oauth2");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
        return authorizationRequest != null ? customizeAuthorizationRequest(authorizationRequest, request) : null;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
        return authorizationRequest != null ? customizeAuthorizationRequest(authorizationRequest, request) : null;
    }

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request) {

        Map<String, Object> additionalParameters = new HashMap<>(authorizationRequest.getAdditionalParameters());

        String lang = request.getParameter("lang");
        String inviteCode = request.getParameter("invite");

        if (lang != null) {
            additionalParameters.put("lang", lang);
        }
        if (inviteCode != null) {
            additionalParameters.put("invite", inviteCode);
        }

        // redirect_uri를 프론트엔드 도메인으로 변경
        String originalRedirectUri = authorizationRequest.getRedirectUri();
        // originalRedirectUri 형식: http://localhost:8080/api/auth/oauth2/callback/google
        // frontendUrl 형식: http://localhost:3000
        // 결과: http://localhost:3000/api/auth/oauth2/callback/google
        String path = originalRedirectUri.substring(originalRedirectUri.indexOf("/api/"));
        String frontendRedirectUri = frontendUrl + path;

        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .redirectUri(frontendRedirectUri)
                .additionalParameters(additionalParameters)
                .build();
    }
}
