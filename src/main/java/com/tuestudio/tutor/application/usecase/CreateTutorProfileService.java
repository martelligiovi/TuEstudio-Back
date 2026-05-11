package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.application.port.TutorRepositoryPort;
import com.tuestudio.tutor.domain.Tutor;
import com.tuestudio.tutor.domain.TutorId;

/**
 * Creates a minimal, inactive Tutor stub and persists it.
 * No @Transactional — the auth application service owns the transaction boundary.
 */
public final class CreateTutorProfileService implements CreateTutorProfileUseCase {

    private final TutorRepositoryPort repository;

    public CreateTutorProfileService(TutorRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public TutorId createStub(TutorId id, String name) {
        Tutor stub = Tutor.stub(id, name);
        repository.save(stub);
        return id;
    }
}
