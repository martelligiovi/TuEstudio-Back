package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.application.port.TutorRepositoryPort;
import com.tuestudio.tutor.domain.Tutor;
import com.tuestudio.tutor.domain.TutorId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateTutorProfileServiceTest {

    @Mock
    TutorRepositoryPort repository;

    CreateTutorProfileService service;

    @BeforeEach
    void setUp() {
        service = new CreateTutorProfileService(repository);
    }

    @Test
    void createStub_callsSaveExactlyOnce() {
        UUID userId = UUID.randomUUID();
        service.createStub(TutorId.of(userId), "Ana");
        verify(repository, times(1)).save(any(Tutor.class));
    }

    @Test
    void createStub_savesStubWithCorrectId() {
        UUID userId = UUID.randomUUID();
        ArgumentCaptor<Tutor> captor = ArgumentCaptor.forClass(Tutor.class);

        service.createStub(TutorId.of(userId), "Ana");
        verify(repository).save(captor.capture());

        assertThat(captor.getValue().id().value()).isEqualTo(userId);
    }

    @Test
    void createStub_savesStubWithCorrectName() {
        UUID userId = UUID.randomUUID();
        ArgumentCaptor<Tutor> captor = ArgumentCaptor.forClass(Tutor.class);

        service.createStub(TutorId.of(userId), "Ana");
        verify(repository).save(captor.capture());

        assertThat(captor.getValue().name()).isEqualTo("Ana");
    }

    @Test
    void createStub_savesStubWithActiveFalse() {
        UUID userId = UUID.randomUUID();
        ArgumentCaptor<Tutor> captor = ArgumentCaptor.forClass(Tutor.class);

        service.createStub(TutorId.of(userId), "Ana");
        verify(repository).save(captor.capture());

        assertThat(captor.getValue().active()).isFalse();
    }

    @Test
    void createStub_returnsTheSameTutorId() {
        UUID userId = UUID.randomUUID();
        TutorId result = service.createStub(TutorId.of(userId), "Ana");
        assertThat(result.value()).isEqualTo(userId);
    }
}
