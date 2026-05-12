package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.application.port.TutorRepositoryPort;
import com.tuestudio.tutor.domain.Methodology;
import com.tuestudio.tutor.domain.Tutor;
import com.tuestudio.tutor.domain.TutorId;
import com.tuestudio.tutor.domain.TutorNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetTutorProfileServiceTest {

    @Mock
    TutorRepositoryPort repository;

    GetTutorProfileService service;

    @BeforeEach
    void setUp() {
        service = new GetTutorProfileService(repository);
    }

    @Test
    void getById_returnsExistingTutor() {
        UUID userId = UUID.randomUUID();
        TutorId tutorId = TutorId.of(userId);
        Tutor tutor = stubTutor(userId, "Ana");

        when(repository.findById(tutorId)).thenReturn(Optional.of(tutor));

        Tutor result = service.getById(tutorId);
        assertThat(result).isSameAs(tutor);
    }

    @Test
    void getById_throwsTutorNotFoundException_whenNotFound() {
        UUID userId = UUID.randomUUID();
        TutorId tutorId = TutorId.of(userId);

        when(repository.findById(tutorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(tutorId))
                .isInstanceOf(TutorNotFoundException.class);
    }

    @Test
    void getById_callsFindByIdWithCorrectId() {
        UUID userId = UUID.randomUUID();
        TutorId tutorId = TutorId.of(userId);
        Tutor tutor = stubTutor(userId, "Ana");

        when(repository.findById(tutorId)).thenReturn(Optional.of(tutor));
        service.getById(tutorId);

        verify(repository, times(1)).findById(tutorId);
    }

    private Tutor stubTutor(UUID id, String name) {
        return new Tutor(
                TutorId.of(id), name, null, null, null, null,
                0.0, 0, null, null, false, 0.0,
                List.of(), new Methodology("", List.of()), List.of(),
                null, List.of(), null
        );
    }
}
