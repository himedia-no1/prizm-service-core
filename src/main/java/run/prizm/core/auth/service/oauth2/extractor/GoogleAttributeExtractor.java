package run.prizm.core.auth.service.oauth2.extractor;

import org.springframework.stereotype.Component;

@Component
public class GoogleAttributeExtractor extends OidcAttributeExtractor {

    @Override
    public boolean supports(String provider) {
        return "google".equalsIgnoreCase(provider);
    }
}