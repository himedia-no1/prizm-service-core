package run.prizm.core.user.service.oauth2.extractor;

import run.prizm.core.user.dto.OAuth2UserData;

import java.util.Map;

public interface OAuth2AttributeExtractor {
    OAuth2UserData extract(Map<String, Object> attributes);
    boolean supports(String provider);
}