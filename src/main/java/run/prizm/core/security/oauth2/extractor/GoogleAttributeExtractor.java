package run.prizm.core.security.oauth2.extractor;

import org.springframework.stereotype.Component;
import run.prizm.core.security.oauth2.OAuth2UserData;

import java.util.Map;

@Component
public class GoogleAttributeExtractor implements OAuth2AttributeExtractor {

    @Override
    public OAuth2UserData extract(Map<String, Object> attributes) {
        String sub = getStringValue(attributes, "sub");
        String email = getStringValue(attributes, "email");
        String name = getStringValue(attributes, "name");
        String picture = getStringValue(attributes, "picture");

        return new OAuth2UserData(sub, email, name, picture);
    }

    @Override
    public boolean supports(String provider) {
        return "google".equalsIgnoreCase(provider);
    }

    private String getStringValue(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        return value != null ? String.valueOf(value) : null;
    }
}