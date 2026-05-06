package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.application.port.TutorRepositoryPort;
import com.tuestudio.tutor.domain.Tutor;
import com.tuestudio.tutor.domain.TutorId;
import com.tuestudio.tutor.domain.TutorNotFoundException;

public final class GetTutorService implements GetTutorUseCase {

    private final TutorRepositoryPort tutorRepository;

    public GetTutorService(TutorRepositoryPort tutorRepository) {
        this.tutorRepository = tutorRepository;
    }

    @Override
    public Tutor getById(TutorId id) {
        return tutorRepository.findById(id)
                .orElseThrow(() -> new TutorNotFoundException(id));
    }
}
