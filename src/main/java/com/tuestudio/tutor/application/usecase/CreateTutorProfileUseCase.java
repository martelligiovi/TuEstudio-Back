package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.domain.TutorId;

/**
 * Creates an inactive Tutor stub for a newly provisioned TEACHER user.
 * Runs inside the caller's transaction (no @Transactional here — auth service owns the TX).
 */
public interface CreateTutorProfileUseCase {
    /**
     * @param id   the TutorId derived from the User's UUID
     * @param name the display name taken from the User
     * @return the TutorId of the created stub (same as the input id)
     */
    TutorId createStub(TutorId id, String name);
}
