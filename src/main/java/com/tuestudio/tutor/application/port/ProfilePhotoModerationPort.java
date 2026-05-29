package com.tuestudio.tutor.application.port;

import com.tuestudio.tutor.application.usecase.ProfilePhotoUpload;

public interface ProfilePhotoModerationPort {
    void assertAllowed(ProfilePhotoUpload upload);
}
