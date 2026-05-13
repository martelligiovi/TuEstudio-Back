package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.application.port.SubjectLookupPort;
import com.tuestudio.tutor.application.port.TutorRepositoryPort;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class SearchTutorsService implements SearchTutorsUseCase {

    private final TutorRepositoryPort tutorRepository;
    private final SubjectLookupPort subjectLookup;

    public SearchTutorsService(TutorRepositoryPort tutorRepository, SubjectLookupPort subjectLookup) {
        this.tutorRepository = tutorRepository;
        this.subjectLookup = subjectLookup;
    }

    @Override
    public List<TutorSummary> search(SearchCriteria criteria) {
        if (criteria.materia() != null && !criteria.materia().isBlank()) {
            Set<UUID> matchingIds = subjectLookup.findIdsMatching(criteria.materia());
            if (matchingIds.isEmpty()) {
                // No subjects match — short-circuit, no DB hit needed
                return List.of();
            }
            return tutorRepository.search(criteria, matchingIds);
        }
        return tutorRepository.search(criteria, null);
    }
}
