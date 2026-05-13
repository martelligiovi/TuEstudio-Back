package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.application.port.SubjectLookupPort;
import com.tuestudio.tutor.application.port.TutorRepositoryPort;
import com.tuestudio.tutor.domain.TutorId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchTutorsServiceTest {

    @Mock TutorRepositoryPort tutorRepository;
    @Mock SubjectLookupPort subjectLookup;
    SearchTutorsService service;

    @BeforeEach
    void setUp() { service = new SearchTutorsService(tutorRepository, subjectLookup); }

    @Test
    void search_withoutMateria_delegatesToRepositoryWithNullMatchingIds() {
        var criteria = new SearchCriteria("UBA", null, null, null, null);
        var expected = List.of(new TutorSummary(
                TutorId.of(UUID.randomUUID()), "María", "UBA",
                List.of(), 2800.0, true, null));
        when(tutorRepository.search(criteria, null)).thenReturn(expected);

        var result = service.search(criteria);

        assertThat(result).isEqualTo(expected);
        verify(tutorRepository).search(criteria, null);
        verifyNoInteractions(subjectLookup);
    }

    @Test
    void search_withMateria_callsSubjectLookupPortOnce() {
        UUID subjectId = UUID.randomUUID();
        var criteria = new SearchCriteria(null, "física", null, null, null);
        when(subjectLookup.findIdsMatching("física")).thenReturn(Set.of(subjectId));
        when(tutorRepository.search(criteria, Set.of(subjectId))).thenReturn(List.of());

        service.search(criteria);

        verify(subjectLookup, times(1)).findIdsMatching("física");
    }

    @Test
    void search_withMateria_andEmptyPortResult_returnsEmptyWithoutHittingRepository() {
        var criteria = new SearchCriteria(null, "quimica", null, null, null);
        when(subjectLookup.findIdsMatching("quimica")).thenReturn(Set.of());

        var result = service.search(criteria);

        assertThat(result).isEmpty();
        verifyNoInteractions(tutorRepository);
    }

    @Test
    void search_returnsEmptyList_whenNoMatch() {
        var criteria = new SearchCriteria("XYZ", null, null, null, null);
        when(tutorRepository.search(criteria, null)).thenReturn(List.of());

        assertThat(service.search(criteria)).isEmpty();
    }
}
