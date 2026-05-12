package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.application.port.TutorRepositoryPort;
import com.tuestudio.tutor.domain.Tutor;
import com.tuestudio.tutor.domain.TutorNotFoundException;
import jakarta.transaction.Transactional;

/**
 * Full-replacement update of a Tutor profile.
 * Reads existing Tutor, replaces all 14 client-editable fields,
 * preserves server-managed fields (rating, reviewsCount),
 * calls recomputeActive() before save.
 * @Transactional — read-then-save must be atomic.
 */
public class UpdateTutorProfileService implements UpdateTutorProfileUseCase {

    private final TutorRepositoryPort repository;

    public UpdateTutorProfileService(TutorRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Tutor update(UpdateTutorProfileCommand c) {
        Tutor existing = repository.findById(c.id())
                .orElseThrow(() -> new TutorNotFoundException(c.id()));

        Tutor updated = new Tutor(
                existing.id(),
                c.name(),
                c.subjectSpecialty(),
                c.university(),
                c.location(),
                c.modalidad(),
                existing.rating(),        // server-managed — preserved
                existing.reviewsCount(),  // server-managed — preserved
                c.bio(),
                c.photoUrl(),
                existing.active(),        // overwritten by recomputeActive() below
                c.hourlyRate(),
                c.subjects(),
                c.methodology(),
                c.schedules(),
                c.schedulesNote(),
                c.plans(),
                c.phoneNumber()
        ).recomputeActive();

        repository.save(updated);
        return updated;
    }
}
