package run.prizm.auth.user.service.oauth2.extractor;

import org.springframework.stereotype.Component;
import run.prizm.auth.user.dto.OAuth2UserData;

import java.util.Map;

@Component
public class GitLabAttributeExtractor extends OidcAttributeExtractor {

    @Override
    public OAuth2UserData extract(Map<String, Object> attributes) {
        String id = getFirstAvailableValue(attributes, "sub", "id");
        String email = getStringValue(attributes, "email");
        String name = getFirstAvailableValue(attributes, "name", "nickname");
        String picture = getFirstAvailableValue(attributes, "picture", "avatar_url");

        return new OAuth2UserData(id, email, name, picture);
    }

    @Override
    public boolean supports(String provider) {
        return "gitlab".equalsIgnoreCase(provider);
    }
}