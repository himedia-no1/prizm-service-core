package run.prizm.auth.user.service.oauth2.extractor;

import run.prizm.auth.user.dto.OAuth2UserData;

import java.util.Map;

public interface OAuth2AttributeExtractor {
    OAuth2UserData extract(Map<String, Object> attributes);
    boolean supports(String provider);
}