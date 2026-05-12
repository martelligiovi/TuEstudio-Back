package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.domain.Tutor;
import com.tuestudio.tutor.domain.TutorId;
import com.tuestudio.tutor.domain.TutorNotFoundException;

/**
 * Retrieves a Tutor profile by its ID.
 * Throws {@link TutorNotFoundException} when no Tutor exists for the given ID.
 */
public interface GetTutorProfileUseCase {
    /**
     * @param id the TutorId (derived from the authenticated user's UUID)
     * @return the Tutor domain object
     * @throws TutorNotFoundException if no Tutor row exists for this id
     */
    Tutor getById(TutorId id);
}
