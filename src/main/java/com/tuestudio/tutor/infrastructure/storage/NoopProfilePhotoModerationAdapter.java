package com.tuestudio.tutor.infrastructure.storage;

import com.tuestudio.tutor.application.port.ProfilePhotoModerationPort;
import com.tuestudio.tutor.application.usecase.ProfilePhotoUpload;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "app.storage.profile-photos.moderation",
        name = "enabled",
        havingValue = "false",
        matchIfMissing = true
)
public class NoopProfilePhotoModerationAdapter implements ProfilePhotoModerationPort {
    @Override
    public void assertAllowed(ProfilePhotoUpload upload) {
        // Moderation is intentionally disabled for local development/tests unless configured.
    }
}
