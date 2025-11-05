package run.prizm.core.auth.service.oauth2.extractor;

import org.springframework.stereotype.Component;
import run.prizm.core.auth.dto.OAuth2UserData;

import java.util.Map;

@Component
public class MicrosoftAttributeExtractor extends OidcAttributeExtractor {

    @Override
    public OAuth2UserData extract(Map<String, Object> attributes) {
        String id = getFirstAvailableValue(attributes, "sub", "oid");
        String email = getFirstAvailableValue(attributes, "email", "preferred_username");
        String name = getStringValue(attributes, "name");
        String picture = getStringValue(attributes, "picture");

        return new OAuth2UserData(id, email, name, picture);
    }

    @Override
    public boolean supports(String provider) {
        return "microsoft".equalsIgnoreCase(provider);
    }
}