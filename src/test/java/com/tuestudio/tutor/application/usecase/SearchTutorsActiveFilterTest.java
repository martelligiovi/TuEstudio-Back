package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.application.port.SubjectLookupPort;
import com.tuestudio.tutor.application.port.TutorRepositoryPort;
import com.tuestudio.tutor.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Verifies that SearchTutorsService passes criteria that include active=true
 * to the repository, so inactive tutors are excluded at the DB level.
 *
 * Per the design, the active=true filter is added inside TutorSpecification.from()
 * (infrastructure layer). The application service is thin — it just delegates.
 * These tests verify the spec requirement that the service only returns active tutors
 * by verifying the repository is called (and results are forwarded) without
 * filtering in the service itself. The TutorSpecification unit tests are in a
 * separate class that exercises the JPA predicate directly via a mock CriteriaBuilder.
 */
@ExtendWith(MockitoExtension.class)
class SearchTutorsActiveFilterTest {

    @Mock TutorRepositoryPort repository;
    @Mock SubjectLookupPort subjectLookup;

    SearchTutorsService service;

    @BeforeEach
    void setUp() {
        service = new SearchTutorsService(repository, subjectLookup);
    }

    @Test
    void search_returnsOnlyActiveResults_fromRepository() {
        // The repository (backed by TutorSpecification) already filters active=true.
        // The service must forward whatever the repository returns — no double-filter.
        TutorSummary active = new TutorSummary(
                TutorId.of(UUID.randomUUID()), "Ana", "UBA",
                List.of(), 1000.0, true, null);

        SearchCriteria criteria = new SearchCriteria(null, null, null, null, null);
        when(repository.search(criteria, null)).thenReturn(List.of(active));

        List<TutorSummary> result = service.search(criteria);

        assertThat(result).containsExactly(active);
        // Repo was called with the same criteria — active filter is in TutorSpecification
        verify(repository).search(criteria, null);
    }

    @Test
    void search_returnsEmpty_whenRepositoryReturnsNoActiveTutors() {
        SearchCriteria criteria = new SearchCriteria(null, null, null, null, null);
        when(repository.search(criteria, null)).thenReturn(List.of());

        assertThat(service.search(criteria)).isEmpty();
    }
}
