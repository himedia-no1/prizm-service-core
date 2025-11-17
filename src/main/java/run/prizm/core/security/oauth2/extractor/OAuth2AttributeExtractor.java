package run.prizm.core.security.oauth2.extractor;

import run.prizm.core.security.oauth2.OAuth2UserData;

import java.util.Map;

public interface OAuth2AttributeExtractor {
    OAuth2UserData extract(Map<String, Object> attributes);

    boolean supports(String provider);
}