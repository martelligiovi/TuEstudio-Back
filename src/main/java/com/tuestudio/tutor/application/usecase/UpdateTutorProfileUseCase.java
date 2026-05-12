package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.domain.Tutor;
import com.tuestudio.tutor.domain.TutorNotFoundException;

/**
 * Full-replacement (PUT semantics) update of a Tutor profile.
 * After applying new field values, recomputeActive() is called before persisting.
 */
public interface UpdateTutorProfileUseCase {
    /**
     * @param command the replacement values for all 14 client-editable fields
     * @return the saved Tutor (post-recomputeActive)
     * @throws TutorNotFoundException if no Tutor exists for command.id()
     */
    Tutor update(UpdateTutorProfileCommand command);
}
