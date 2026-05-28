package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.domain.Tutor;
import com.tuestudio.tutor.domain.TutorId;

public interface UpdateTutorProfilePhotoUseCase {
    Tutor updatePhoto(TutorId id, ProfilePhotoUpload upload);
}
