package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.application.port.TutorRepositoryPort;
import com.tuestudio.tutor.domain.*;
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
class GetTutorServiceTest {

    @Mock TutorRepositoryPort tutorRepository;
    GetTutorService service;

    @BeforeEach
    void setUp() { service = new GetTutorService(tutorRepository); }

    @Test
    void getById_returnsTutor_whenExists() {
        var id = TutorId.generate();
        var tutor = new Tutor(id, "María", "Matemáticas", "UBA", "CABA", "Virtual",
                4.9, 10, "bio", null, true, 2800.0,
                List.of(), new Methodology("", List.of()), List.of(), null, List.of());
        when(tutorRepository.findById(id)).thenReturn(Optional.of(tutor));

        assertThat(service.getById(id)).isEqualTo(tutor);
    }

    @Test
    void getById_throws_whenNotFound() {
        var id = TutorId.generate();
        when(tutorRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(TutorNotFoundException.class);
    }
}
