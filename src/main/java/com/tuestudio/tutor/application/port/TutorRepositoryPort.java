package com.tuestudio.tutor.application.port;

import com.tuestudio.tutor.application.usecase.SearchCriteria;
import com.tuestudio.tutor.application.usecase.TutorSummary;
import com.tuestudio.tutor.domain.Tutor;
import com.tuestudio.tutor.domain.TutorId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TutorRepositoryPort {
    /**
     * Searches tutors matching the given criteria.
     *
     * @param criteria          the search criteria
     * @param matchingSubjectIds pre-resolved subject UUIDs for materia filter, or {@code null} if
     *                          no materia filter is active. Empty set results in empty response.
     */
    List<TutorSummary> search(SearchCriteria criteria, Set<UUID> matchingSubjectIds);
    Optional<Tutor> findById(TutorId id);
    boolean existsById(TutorId id);
    void save(Tutor tutor);
}
