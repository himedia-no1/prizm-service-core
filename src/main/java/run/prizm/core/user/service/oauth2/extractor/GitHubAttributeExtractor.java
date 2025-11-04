package run.prizm.core.user.service.oauth2.extractor;

import org.springframework.stereotype.Component;
import run.prizm.core.user.dto.OAuth2UserData;

import java.util.Map;

@Component
public class GitHubAttributeExtractor implements OAuth2AttributeExtractor {

    @Override
    public OAuth2UserData extract(Map<String, Object> attributes) {
        String id = getStringValue(attributes, "id");
        String email = getStringValue(attributes, "email");
        String name = getFirstAvailableValue(attributes, "name", "login");
        String avatar = getStringValue(attributes, "avatar_url");

        return new OAuth2UserData(id, email, name, avatar);
    }

    @Override
    public boolean supports(String provider) {
        return "github".equalsIgnoreCase(provider);
    }

    private String getStringValue(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        return value != null ? String.valueOf(value) : null;
    }

    private String getFirstAvailableValue(Map<String, Object> attributes, String... keys) {
        for (String key : keys) {
            Object value = attributes.get(key);
            if (value != null) {
                return String.valueOf(value);
            }
        }
        return null;
    }
}