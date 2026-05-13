package com.tuestudio.subject.application.usecase;

import com.tuestudio.subject.application.port.SubjectRepositoryPort;
import com.tuestudio.subject.domain.Subject;
import com.tuestudio.subject.domain.SubjectAlreadyExistsException;
import com.tuestudio.subject.domain.SubjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateSubjectServiceTest {

    @Mock SubjectRepositoryPort repo;

    CreateSubjectService service;

    @BeforeEach
    void setUp() {
        service = new CreateSubjectService(repo);
    }

    @Test
    void create_withUniqueName_returnsSavedSubject() {
        when(repo.existsByCanonicalNameIgnoreCase("Álgebra")).thenReturn(false);
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Subject result = service.create("Álgebra");

        assertThat(result.canonicalName()).isEqualTo("Álgebra");
    }

    @Test
    void create_withDuplicateName_throwsAlreadyExists() {
        when(repo.existsByCanonicalNameIgnoreCase("Álgebra")).thenReturn(true);

        assertThatThrownBy(() -> service.create("Álgebra"))
                .isInstanceOf(SubjectAlreadyExistsException.class);

        verify(repo, never()).save(any());
    }

    @Test
    void create_invokesSaveOnce() {
        when(repo.existsByCanonicalNameIgnoreCase(anyString())).thenReturn(false);
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.create("Física");

        verify(repo, times(1)).save(any());
    }

    @Test
    void create_isCaseInsensitiveForUniqueness() {
        when(repo.existsByCanonicalNameIgnoreCase("álgebra")).thenReturn(true);

        assertThatThrownBy(() -> service.create("álgebra"))
                .isInstanceOf(SubjectAlreadyExistsException.class);
    }

    @Test
    void create_newSubjectHasEmptyAliases() {
        when(repo.existsByCanonicalNameIgnoreCase(anyString())).thenReturn(false);
        ArgumentCaptor<Subject> captor = ArgumentCaptor.forClass(Subject.class);
        when(repo.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.create("Química");

        assertThat(captor.getValue().aliases()).isEmpty();
    }
}
