package com.tuestudio.tutor.application.port;

import com.tuestudio.tutor.application.usecase.ProfilePhotoUpload;
import com.tuestudio.tutor.domain.TutorId;

public interface ProfilePhotoStoragePort {
    String store(TutorId tutorId, ProfilePhotoUpload upload);
}
