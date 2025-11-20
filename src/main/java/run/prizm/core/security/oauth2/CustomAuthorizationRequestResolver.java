package run.prizm.core.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import run.prizm.core.properties.UrlProperties;

import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultResolver;
    private final UrlProperties urlProperties;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository,
                                              UrlProperties urlProperties) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/api/auth/oauth2");
        this.urlProperties = urlProperties;
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

        String inviteCode = request.getParameter("invite");

        if (inviteCode != null) {
            additionalParameters.put("invite", inviteCode);
        }

        String originalRedirectUri = authorizationRequest.getRedirectUri();
        String frontendRedirectUri = resolveRedirectUri(originalRedirectUri);

        return OAuth2AuthorizationRequest.from(authorizationRequest)
                                         .redirectUri(frontendRedirectUri)
                                         .additionalParameters(additionalParameters)
                                         .build();
    }

    private String resolveRedirectUri(String originalRedirectUri) {
        String frontendUrl = urlProperties.getWebUserUrl();
        if (!StringUtils.hasText(frontendUrl)) {
            return originalRedirectUri;
        }

        int apiIndex = originalRedirectUri.indexOf("/api/");
        String path = apiIndex >= 0 ? originalRedirectUri.substring(apiIndex) : originalRedirectUri;
        return frontendUrl + path;
    }
}
