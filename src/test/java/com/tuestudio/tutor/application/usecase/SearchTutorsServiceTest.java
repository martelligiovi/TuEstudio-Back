package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.application.port.TutorRepositoryPort;
import com.tuestudio.tutor.domain.TutorId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchTutorsServiceTest {

    @Mock TutorRepositoryPort tutorRepository;
    SearchTutorsService service;

    @BeforeEach
    void setUp() { service = new SearchTutorsService(tutorRepository); }

    @Test
    void search_delegatesToRepository() {
        var criteria = new SearchCriteria("UBA", null, null, null, null);
        var expected = List.of(new TutorSummary(
                TutorId.of(UUID.randomUUID()), "María", "UBA",
                List.of("Matemáticas"), 2800.0, true, null));
        when(tutorRepository.search(criteria)).thenReturn(expected);

        var result = service.search(criteria);

        assertThat(result).isEqualTo(expected);
        verify(tutorRepository).search(criteria);
    }

    @Test
    void search_returnsEmptyList_whenNoMatch() {
        var criteria = new SearchCriteria("XYZ", null, null, null, null);
        when(tutorRepository.search(criteria)).thenReturn(List.of());

        assertThat(service.search(criteria)).isEmpty();
    }
}
