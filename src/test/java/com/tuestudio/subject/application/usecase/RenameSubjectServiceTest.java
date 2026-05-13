package com.tuestudio.subject.application.usecase;

import com.tuestudio.subject.application.port.SubjectRepositoryPort;
import com.tuestudio.subject.domain.Subject;
import com.tuestudio.subject.domain.SubjectAlreadyExistsException;
import com.tuestudio.subject.domain.SubjectId;
import com.tuestudio.subject.domain.SubjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RenameSubjectServiceTest {

    @Mock SubjectRepositoryPort repo;

    RenameSubjectService service;

    @BeforeEach
    void setUp() {
        service = new RenameSubjectService(repo);
    }

    @Test
    void rename_existingSubject_updates() {
        SubjectId id = SubjectId.newId();
        Subject existing = Subject.create(id, "Álgebra");
        when(repo.findById(id)).thenReturn(Optional.of(existing));
        when(repo.existsByCanonicalNameIgnoreCase("Álgebra Lineal")).thenReturn(false);
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Subject result = service.rename(id, "Álgebra Lineal");

        assertThat(result.canonicalName()).isEqualTo("Álgebra Lineal");
    }

    @Test
    void rename_unknownId_throwsNotFound() {
        SubjectId id = SubjectId.newId();
        when(repo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.rename(id, "Nuevo"))
                .isInstanceOf(SubjectNotFoundException.class);
    }

    @Test
    void rename_toExistingCanonicalOfOther_throwsAlreadyExists() {
        SubjectId id = SubjectId.newId();
        Subject existing = Subject.create(id, "Álgebra");
        when(repo.findById(id)).thenReturn(Optional.of(existing));
        when(repo.existsByCanonicalNameIgnoreCase("Física")).thenReturn(true);

        assertThatThrownBy(() -> service.rename(id, "Física"))
                .isInstanceOf(SubjectAlreadyExistsException.class);
    }

    @Test
    void rename_toSelfCanonical_isNoOp() {
        SubjectId id = SubjectId.newId();
        Subject existing = Subject.create(id, "Álgebra");
        when(repo.findById(id)).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Subject result = service.rename(id, "Álgebra");

        assertThat(result.canonicalName()).isEqualTo("Álgebra");
        verify(repo, never()).existsByCanonicalNameIgnoreCase(any());
    }
}
