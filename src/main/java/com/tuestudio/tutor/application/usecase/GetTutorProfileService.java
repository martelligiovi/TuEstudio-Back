package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.application.port.TutorRepositoryPort;
import com.tuestudio.tutor.domain.Tutor;
import com.tuestudio.tutor.domain.TutorId;
import com.tuestudio.tutor.domain.TutorNotFoundException;

/**
 * Read-only retrieval of a Tutor profile.
 * No @Transactional — read-only operation; no need.
 */
public final class GetTutorProfileService implements GetTutorProfileUseCase {

    private final TutorRepositoryPort repository;

    public GetTutorProfileService(TutorRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Tutor getById(TutorId id) {
        return repository.findById(id)
                .orElseThrow(() -> new TutorNotFoundException(id));
    }
}
