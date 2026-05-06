package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.application.port.TutorRepositoryPort;
import java.util.List;

public final class SearchTutorsService implements SearchTutorsUseCase {

    private final TutorRepositoryPort tutorRepository;

    public SearchTutorsService(TutorRepositoryPort tutorRepository) {
        this.tutorRepository = tutorRepository;
    }

    @Override
    public List<TutorSummary> search(SearchCriteria criteria) {
        return tutorRepository.search(criteria);
    }
}
