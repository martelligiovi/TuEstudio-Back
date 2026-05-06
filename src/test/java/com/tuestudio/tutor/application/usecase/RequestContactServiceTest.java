package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.application.port.ContactRequestRepositoryPort;
import com.tuestudio.tutor.application.port.TutorRepositoryPort;
import com.tuestudio.tutor.domain.TutorId;
import com.tuestudio.tutor.domain.TutorNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestContactServiceTest {

    @Mock TutorRepositoryPort tutorRepository;
    @Mock ContactRequestRepositoryPort contactRepository;
    RequestContactService service;

    @BeforeEach
    void setUp() { service = new RequestContactService(tutorRepository, contactRepository); }

    @Test
    void request_savesContactRequest_whenTutorExists() {
        var tutorId = TutorId.generate();
        when(tutorRepository.existsById(tutorId)).thenReturn(true);

        service.request(new ContactRequestCommand(tutorId, "Juan", "1122334455"));

        verify(contactRepository).save(any());
    }

    @Test
    void request_throws_whenTutorNotFound() {
        var tutorId = TutorId.generate();
        when(tutorRepository.existsById(tutorId)).thenReturn(false);

        assertThatThrownBy(() -> service.request(new ContactRequestCommand(tutorId, "Juan", "1122334455")))
                .isInstanceOf(TutorNotFoundException.class);

        verify(contactRepository, never()).save(any());
    }
}
