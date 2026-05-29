package com.tuestudio.tutor.infrastructure.storage;

import com.google.cloud.vision.v1.Likelihood;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage.profile-photos.moderation")
public record ProfilePhotoModerationProperties(
        boolean enabled,
        Likelihood adultThreshold,
        Likelihood violenceThreshold,
        boolean rejectRacy,
        Likelihood racyThreshold
) {
    public ProfilePhotoModerationProperties {
        adultThreshold = adultThreshold == null ? Likelihood.LIKELY : adultThreshold;
        violenceThreshold = violenceThreshold == null ? Likelihood.VERY_LIKELY : violenceThreshold;
        racyThreshold = racyThreshold == null ? Likelihood.VERY_LIKELY : racyThreshold;
    }
}
