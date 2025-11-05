package run.prizm.core.auth.service.oauth2.extractor;

import run.prizm.core.auth.dto.OAuth2UserData;

import java.util.Map;

public abstract class OidcAttributeExtractor implements OAuth2AttributeExtractor {

    @Override
    public OAuth2UserData extract(Map<String, Object> attributes) {
        String id = getStringValue(attributes, getIdClaim());
        String email = getStringValue(attributes, getEmailClaim());
        String name = getStringValue(attributes, getNameClaim());
        String picture = getStringValue(attributes, getPictureClaim());

        return new OAuth2UserData(id, email, name, picture);
    }

    protected String getIdClaim() {
        return "sub";
    }

    protected String getEmailClaim() {
        return "email";
    }

    protected String getNameClaim() {
        return "name";
    }

    protected String getPictureClaim() {
        return "picture";
    }

    protected String getStringValue(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        return value != null ? String.valueOf(value) : null;
    }

    protected String getFirstAvailableValue(Map<String, Object> attributes, String... keys) {
        for (String key : keys) {
            Object value = attributes.get(key);
            if (value != null) {
                return String.valueOf(value);
            }
        }
        return null;
    }
}