package run.prizm.core.user.dto;

import run.prizm.core.common.constraint.Language;
import run.prizm.core.user.constraint.UserAuthProvider;
import java.time.Instant;

public record UserProfileResponse(
        String profileImage,
        String name,
        String email,
        UserAuthProvider authProvider,
        Language language,
        Instant createdAt
) {}
